package ro.aenigma.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.extensions.AddressExtensions.isValidAddress
import ro.aenigma.crypto.extensions.Base64Extensions.isValidBase64
import ro.aenigma.crypto.extensions.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.extensions.PublicKeyExtensions.isValidPublicKey
import ro.aenigma.crypto.extensions.PublicKeyExtensions.publicKeyMatchAddress
import ro.aenigma.crypto.extensions.SignatureExtensions.getStringDataFromSignature
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.network.EnigmaApi
import ro.aenigma.models.ArticleDto
import ro.aenigma.models.CreatedSharedDataDto
import ro.aenigma.models.GroupDataDto
import ro.aenigma.models.GuardDto
import ro.aenigma.models.NeighborhoodDto
import ro.aenigma.models.SharedDataDto
import ro.aenigma.models.SharedDataCreate
import ro.aenigma.models.TorCheckDto
import ro.aenigma.models.VertexDto
import ro.aenigma.models.extensions.NeighborhoodExtensions.normalizeHostname
import ro.aenigma.services.RetrofitProvider
import ro.aenigma.util.Constants.Companion.ARTICLES_INDEX_URL_TEMPLATE
import ro.aenigma.util.Constants.Companion.DEFAULT_LANGUAGE_CODE
import ro.aenigma.util.ContextExtensions.createTempCacheFile
import ro.aenigma.util.FileExtensions.asBufferedRequestBody
import ro.aenigma.util.ResponseBodyExtensions.saveToFile
import ro.aenigma.util.SerializerExtensions.toCanonicalJson
import ro.aenigma.util.StringExtensions.fromJson
import ro.aenigma.util.StringExtensions.getBaseUrl
import ro.aenigma.util.StringExtensions.getTagQueryParameter
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val retrofitProvider: RetrofitProvider,
    private val signatureService: SignatureService
) {
    companion object {
        @JvmStatic
        private suspend fun getSharedData(
            api: EnigmaApi,
            tag: String,
            expectedPublisherAddress: String?
        ): SharedDataDto? {
            try {
                val response = api.getSharedData(tag)
                val body = response.body() ?: return null
                if (body.publicKey == null || body.data == null) {
                    return null
                }
                val expectedPublishedMatched = expectedPublisherAddress == null ||
                        (body.publicKey.getAddressFromPublicKey() == expectedPublisherAddress)
                if (response.code() != 200
                    || body.tag != tag
                    || !expectedPublishedMatched
                    || !CryptoProvider.verifyEx(body.publicKey, body.data)
                ) {
                    return null
                }
                return body
            } catch (_: Exception) {
                return null
            }
        }

        @JvmStatic
        private fun validateGroupData(
            groupDataDto: GroupDataDto,
            existentGroup: GroupDataDto?,
            expectedPublisherAddress: String
        ): GroupDataDto? {
            if (groupDataDto.name.isNullOrBlank()
                || groupDataDto.address.isNullOrBlank()
                || groupDataDto.members.isNullOrEmpty()
                || groupDataDto.admins.isNullOrEmpty()
                || groupDataDto.nonce == null
                || groupDataDto.members.any { item -> item.address != item.publicKey.getAddressFromPublicKey() }
                || groupDataDto.admins.any { item -> !item.isValidAddress() }
            ) {
                return null
            }

            val publisherIsAdmin = groupDataDto.admins.contains(expectedPublisherAddress)
            val newGroup = existentGroup == null
            val nonceIsGreaterThanPrevious =
                !newGroup && groupDataDto.nonce > (existentGroup.nonce ?: Long.MAX_VALUE)
            val adminModifiesGroup = !newGroup && publisherIsAdmin && nonceIsGreaterThanPrevious
            return when (publisherIsAdmin && (newGroup || adminModifiesGroup)) {
                true -> groupDataDto
                else -> null
            }
        }

        @JvmStatic
        private suspend fun verifyServerInfo(
            api: EnigmaApi,
            expectedAddress: String? = null
        ): GuardDto? {
            val response = api.getServerInfo()
            val serverInfoBody = response.body() ?: return null
            if (response.code() != 200) {
                return null
            } else {
                val address = serverInfoBody.address ?: return null
                if (!expectedAddress.isNullOrBlank() && address != expectedAddress) {
                    return null
                }
                val graphVersion = serverInfoBody.graphVersion ?: return null
                val vertexResponse = api.getVertex(address)
                if (vertexResponse.code() != 200) {
                    return null
                }
                val vertexBody = vertexResponse.body() ?: return null
                if (vertexBody.neighborhood?.address != address) {
                    return null
                }
                val validatedVertex = validateVertex(vertexBody) ?: return null
                return GuardDto(
                    publicKey = validatedVertex.publicKey!!,
                    address = validatedVertex.neighborhood!!.address!!,
                    hostname = validatedVertex.neighborhood.hostname,
                    onionService = validatedVertex.neighborhood.onionService,
                    graphVersion = graphVersion
                )
            }
        }

        @JvmStatic
        private fun validateVertex(vertexDto: VertexDto?): VertexDto? {
            if (vertexDto == null) {
                return null
            }
            if (!vertexDto.publicKey.isValidPublicKey()) {
                return null
            }
            if (vertexDto.neighborhood?.neighbors?.all { item -> item.isValidAddress() } != true) {
                return null
            }
            if (!vertexDto.neighborhood.address.isValidAddress()) {
                return null
            }
            if (!vertexDto.publicKey.publicKeyMatchAddress(vertexDto.neighborhood.address)) {
                return null
            }
            if (!vertexDto.signedData.isValidBase64() || !CryptoProvider.verifyEx(
                    vertexDto.publicKey!!,
                    vertexDto.signedData!!
                )
            ) {
                return null
            }
            val serializedNeighborhood =
                vertexDto.signedData.getStringDataFromSignature(vertexDto.publicKey) ?: return null
            val neighborhood =
                serializedNeighborhood.fromJson<NeighborhoodDto>()?.normalizeHostname()
                    ?: return null
            return VertexDto(
                address = vertexDto.address,
                publicKey = vertexDto.publicKey,
                signedData = vertexDto.signedData,
                neighborhood = neighborhood
            )
        }
    }

    suspend fun getServerInfo(): GuardDto? {
        return try {
            verifyServerInfo(retrofitProvider.getApi() ?: return null)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getVertices(): List<VertexDto> {
        return try {
            val response = retrofitProvider.getApi()?.getVertices() ?: return listOf()
            val body = response.body()

            if (response.code() != 200 || body == null) {
                listOf()
            } else {
                body.mapNotNull { vertex -> validateVertex(vertex) }
            }
        } catch (_: Exception) {
            listOf()
        }
    }

    suspend fun createSharedData(
        data: ByteArray,
        passphrase: ByteArray?,
        accessCount: Int = 1
    ): CreatedSharedDataDto? {
        try {
            val out = (if (passphrase != null) CryptoProvider.encrypt(data, passphrase) else data)
                ?: return null
            val signature = signatureService.sign(out)
            signature.signedData ?: return null
            signature.publicKey ?: return null
            val sharedDataCreate =
                SharedDataCreate(signature.publicKey, signature.signedData, accessCount)
            val response =
                retrofitProvider.getApi()?.createSharedData(sharedDataCreate) ?: return null
            val body = response.body()
            if (response.code() != 200 || body?.tag == null || body.resourceUrl == null) {
                return null
            }
            return body
        } catch (_: Exception) {
            return null
        }
    }

    suspend fun incrementSharedDataAccessCount(url: String): Boolean {
        try {
            return retrofitProvider.getApi(url.getBaseUrl() ?: return false)
                ?.incrementSharedDataAccessCount(url.getTagQueryParameter() ?: return false)
                ?.code() == 200
        } catch (_: Exception) {
            return false
        }
    }

    suspend fun getSharedData(url: String, expectedPublisherAddress: String?): SharedDataDto? {
        val tag = url.getTagQueryParameter() ?: return null
        val baseUrl = url.getBaseUrl() ?: return null
        return getSharedData(
            retrofitProvider.getApi(baseUrl) ?: return null,
            tag,
            expectedPublisherAddress
        )
    }

    suspend fun getGroupData(
        url: String,
        existentGroup: GroupDataDto?,
        key: ByteArray,
        expectedPublisherAddress: String
    ): GroupDataDto? {
        var groupDataFile: File? = null
        return try {
            groupDataFile = context.createTempCacheFile(null)
            if (getEncryptedFile(url, key, groupDataFile)) {
                val groupData = groupDataFile.readText().fromJson<GroupDataDto>() ?: return null
                validateGroupData(groupData, existentGroup, expectedPublisherAddress)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            groupDataFile?.delete()
        }
    }

    suspend fun postGroupData(
        groupData: GroupDataDto,
        accessCount: Int,
        key: ByteArray,
        usingTor: Boolean,
        onProgress: (Int) -> Unit = { }
    ): CreatedSharedDataDto? {
        var groupDataFile: File? = null
        return try {
            val serializedGroupData = groupData.toCanonicalJson()?.toByteArray() ?: return null

            groupDataFile = context.createTempCacheFile(null)
            groupDataFile.outputStream().buffered()
                .use { outputStream -> outputStream.write(serializedGroupData) }

            return postEncryptedFile(groupDataFile, accessCount, key, usingTor, onProgress)
        } catch (_: Exception) {
            null
        } finally {
            groupDataFile?.delete()
        }
    }

    suspend fun postFile(
        file: File,
        accessCount: Int = 1,
        usingTor: Boolean,
        onProgress: (Int) -> Unit = { }
    ): CreatedSharedDataDto? {
        return try {
            val filePart = MultipartBody.Part.createFormData(
                name = "file",
                filename = file.name,
                body = file.asBufferedRequestBody("application/octet-stream", usingTor, onProgress)
            )
            val countPart = accessCount
                .toString()
                .toRequestBody("text/plain".toMediaType())
            val response =
                retrofitProvider.getApi()?.postFile(file = filePart, maxAccessCount = countPart)
                    ?: return null
            val body = response.body()
            if (response.code() != 200 || body?.tag == null || body.resourceUrl == null) {
                null
            } else {
                body
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun postEncryptedFile(
        file: File,
        accessCount: Int,
        key: ByteArray,
        usingTor: Boolean,
        onProgress: (Int) -> Unit = { }
    ): CreatedSharedDataDto? {
        var encryptedFile: File? = null
        return try {
            encryptedFile = context.createTempCacheFile(null)
            if (CryptoProvider.encrypt(file, encryptedFile, key)) {
                postFile(encryptedFile, accessCount, usingTor, onProgress)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            encryptedFile?.delete()
        }
    }

    suspend fun incrementFileAccessCount(url: String): Boolean {
        try {
            return retrofitProvider.getApi(url.getBaseUrl() ?: return false)
                ?.incrementFileAccessCount(url.getTagQueryParameter() ?: return false)
                ?.code() == 200
        } catch (_: Exception) {
            return false
        }
    }

    suspend fun getServerInfo(url: String, expectedAddress: String? = null): GuardDto? {
        return try {
            val baseUrl = url.getBaseUrl() ?: return null
            val api = retrofitProvider.getApi(baseUrl) ?: return null
            verifyServerInfo(api, expectedAddress)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getFile(url: String, outFile: File): Boolean {
        return try {
            val tag = url.getTagQueryParameter() ?: return false
            val response = retrofitProvider.getApi(url.getBaseUrl() ?: return false)?.getFile(tag)
                ?: return false
            val body = response.body()
            if (response.code() != 200 || body == null) {
                false
            } else {
                body.saveToFile(outFile)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getEncryptedFile(url: String, key: ByteArray, outFile: File): Boolean {
        var encryptedFile: File? = null
        return try {
            encryptedFile = context.createTempCacheFile(null)
            getFile(url, encryptedFile) && CryptoProvider.decrypt(encryptedFile, outFile, key)
        } catch (_: Exception) {
            false
        } finally {
            encryptedFile?.delete()
        }
    }

    private suspend fun requestArticles(url: String): List<ArticleDto> {
        return try {
            val response =
                retrofitProvider.getApi(url.getBaseUrl() ?: return listOf())?.getArticlesIndex(url)
                    ?: return listOf()
            val body = response.body()
            if (response.code() != 200 || body == null) {
                listOf()
            } else {
                body
            }
        } catch (_: Exception) {
            listOf()
        }
    }

    suspend fun getArticles(
        url: String,
        fallback: String = String.format(ARTICLES_INDEX_URL_TEMPLATE, DEFAULT_LANGUAGE_CODE)
    ): List<ArticleDto> {
        val response = requestArticles(url)
        return response.ifEmpty {
            requestArticles(fallback)
        }
    }

    suspend fun getText(url: String): String? {
        return try {
            val response =
                retrofitProvider.getApi(url.getBaseUrl() ?: return null)?.getText(url)
                    ?: return null
            val body = response.body() ?: return null
            if (response.code() != 200) {
                null
            } else {
                body
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun checkTor(url: String): TorCheckDto? {
        return try {
            val response = retrofitProvider.getApi(url.getBaseUrl() ?: return null)?.checkTor(url)
                ?: return null
            val body = response.body() ?: return null
            if (response.code() != 200) {
                null
            } else {
                body
            }
        } catch (_: Exception) {
            null
        }
    }
}
