package ro.aenigma.data

import ro.aenigma.models.CreatedSharedData
import ro.aenigma.models.ServerInfo
import ro.aenigma.models.SharedData
import ro.aenigma.models.SharedDataCreate
import ro.aenigma.models.Vertex
import retrofit2.Response
import ro.aenigma.crypto.extensions.AddressExtensions.isValidAddress
import ro.aenigma.crypto.extensions.Base64Extensions.isValidBase64
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.extensions.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.extensions.PublicKeyExtensions.isValidPublicKey
import ro.aenigma.crypto.extensions.PublicKeyExtensions.publicKeyMatchAddress
import ro.aenigma.crypto.extensions.SignatureExtensions.getDataFromSignature
import ro.aenigma.crypto.extensions.SignatureExtensions.getStringDataFromSignature
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.network.EnigmaApi
import ro.aenigma.models.GroupData
import ro.aenigma.models.Neighborhood
import ro.aenigma.services.RetrofitProvider
import ro.aenigma.util.SerializerExtensions.fromJson
import ro.aenigma.util.getBaseUrl
import ro.aenigma.util.getTagQueryParameter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val retrofitProvider: RetrofitProvider,
    private val signatureService: SignatureService
) {
    companion object {
        @JvmStatic
        private suspend fun getSharedData(api: EnigmaApi, tag: String): SharedData? {
            try {
                val response = api.getSharedData(tag)
                val body = response.body() ?: return null
                if (body.publicKey == null || body.data == null) {
                    return null
                }
                if (response.code() != 200 || body.tag != tag || !CryptoProvider.verifyEx(
                        body.publicKey,
                        body.data
                    )
                ) {
                    return null
                }
                return body
            } catch (_: Exception) {
                return null
            }
        }
    }

    suspend fun getServerInfo(): Response<ServerInfo?> {
        return retrofitProvider.getApi().getServerInfo()
    }

    suspend fun getVertices(): List<Vertex> {
        val response = retrofitProvider.getApi().getVertices()
        val body = response.body()

        if (response.code() != 200 || body == null) {
            return listOf()
        }

        return body.mapNotNull { vertex -> validateVertex(vertex, false) }
    }

    suspend fun createSharedData(data: ByteArray, accessCount: Int = 1): CreatedSharedData? {
        try {
            val signature = signatureService.sign(data)
            signature.signedData ?: return null
            signature.publicKey ?: return null
            val sharedDataCreate =
                SharedDataCreate(signature.publicKey, signature.signedData, accessCount)
            val response = retrofitProvider.getApi().createSharedData(sharedDataCreate)
            val body = response.body()
            if (response.code() != 200 || body?.tag == null || body.resourceUrl == null) {
                return null
            }
            return body
        } catch (_: Exception) {
            return null
        }
    }

    suspend fun getSharedDataByUrl(url: String): SharedData? {
        val tag = url.getTagQueryParameter()?: return null
        val baseUrl = url.getBaseUrl()
        return getSharedData(retrofitProvider.getApi(baseUrl), tag)
    }

    suspend fun getGroupDataByUrl(
        url: String,
        existentGroup: GroupData?,
        key: ByteArray
    ): GroupData? {
        try {
            val response = getSharedDataByUrl(url) ?: return null
            val data =
                response.data.getDataFromSignature(response.publicKey ?: return null) ?: return null
            val groupData =
                String(CryptoProvider.decrypt(key, data) ?: return null).fromJson<GroupData>()
                    ?: return null
            return validateGroupData(groupData, response, existentGroup)
        } catch (_: Exception) {
            return null
        }
    }

    private fun validateGroupData(
        groupData: GroupData,
        sharedData: SharedData,
        existentGroup: GroupData?
    ): GroupData? {
        if (groupData.name == null || groupData.address == null || groupData.members == null
            || groupData.nonce == null
            || groupData.members.isEmpty()
            || groupData.members.any { item -> item.address != item.publicKey.getAddressFromPublicKey() }
            || groupData.admins == null || groupData.admins.isEmpty()
            || groupData.admins.any { item -> !item.isValidAddress() }
        ) {
            return null
        }
        val publisherAddress = sharedData.publicKey.getAddressFromPublicKey()
        val publisherIsAdmin = groupData.admins.contains(publisherAddress)
        val newGroup = existentGroup == null
        val nonceIsGreaterThanPrevious = !newGroup && groupData.nonce > (existentGroup.nonce ?: Long.MAX_VALUE)
        val adminModifiesGroup =
            !newGroup && existentGroup.admins?.contains(publisherAddress) == true
                    && nonceIsGreaterThanPrevious
        return when {
            publisherIsAdmin && (newGroup || adminModifiesGroup) -> groupData
            else -> null
        }
    }

    suspend fun getVertex(address: String, isLeaf: Boolean, publicKey: String? = null): Vertex? {
        val response = retrofitProvider.getApi().getVertex(address)
        val vertex = response.body()

        if (response.code() != 200 || vertex == null) {
            return null
        }

        return validateVertex(vertex, isLeaf, publicKey)
    }

    private fun validateVertex(
        vertex: Vertex?,
        isLeaf: Boolean,
        publicKey: String? = null
    ): Vertex? {
        if (vertex == null) {
            return null
        }
        val key = if (publicKey.isNullOrBlank()) vertex.publicKey else publicKey
        if (!key.isValidPublicKey()) {
            return null
        }
        if ((isLeaf && vertex.neighborhood?.neighbors?.count() != 1)
            || vertex.neighborhood?.neighbors?.all { item -> item.isValidAddress() } != true
        ) {
            return null
        }
        if (!vertex.neighborhood.address.isValidAddress()) {
            return null
        }
        if (!isLeaf && !key.publicKeyMatchAddress(vertex.neighborhood.address)) {
            return null
        }
        if (!vertex.signedData.isValidBase64() || !CryptoProvider.verifyEx(
                key!!,
                vertex.signedData!!
            )
        ) {
            return null
        }
        val serializedNeighborhood =
            vertex.signedData.getStringDataFromSignature(key) ?: return null
        val neighborhood = serializedNeighborhood.fromJson<Neighborhood>() ?: return null
        return Vertex(vertex.publicKey, vertex.signedData, neighborhood)
    }
}
