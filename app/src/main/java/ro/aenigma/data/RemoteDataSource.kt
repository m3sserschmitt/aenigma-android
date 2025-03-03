package ro.aenigma.data

import ro.aenigma.data.network.EnigmaApi
import ro.aenigma.models.CreatedSharedData
import ro.aenigma.models.ServerInfo
import ro.aenigma.models.SharedData
import ro.aenigma.models.SharedDataCreate
import ro.aenigma.models.Vertex
import retrofit2.Response
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.PublicKeyExtensions.isValidPublicKey
import ro.aenigma.crypto.getStringDataFromSignature
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.models.GroupData
import ro.aenigma.util.fromJson
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val enigmaApi: EnigmaApi,
    private val signatureService: SignatureService
) {

    suspend fun getServerInfo(): Response<ServerInfo?> {
        return enigmaApi.getServerInfo()
    }

    suspend fun getVertices(): Response<List<Vertex>?> {
        return enigmaApi.getVertices()
    }

    suspend fun createSharedData(data: String, accessCount: Int = 1): CreatedSharedData? {
        try {
            val signature = signatureService.sign(data.toByteArray()) ?: return null
            val sharedDataCreate = SharedDataCreate(signature.first, signature.second, accessCount)
            val response = enigmaApi.createSharedData(sharedDataCreate)
            val body = response.body()
            if (response.code() != 200 || body?.tag == null || body.resourceUrl == null) {
                return null
            }
            return body
        } catch (_: Exception) {
            return null
        }
    }

    suspend fun getSharedData(tag: String): SharedData? {
        try {
            val response = enigmaApi.getSharedData(tag)
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

    suspend fun getGroupData(tag: String): GroupData? {
        try {
            val response = getSharedData(tag) ?: return null
            val encryptedDataList =
                response.data.getStringDataFromSignature(response.publicKey!!)
                    .fromJson<List<String>>()
                    ?: return null

            var decryptedContent: ByteArray? = null
            for (data in encryptedDataList) {
                decryptedContent = CryptoProvider.decryptEx(data)
                if (decryptedContent != null) {
                    break
                }
            }
            if (decryptedContent == null) {
                return null
            }
            val groupData =
                decryptedContent.toString(Charsets.UTF_8).fromJson<GroupData>() ?: return null
            if (groupData.name == null || groupData.address == null || groupData.members == null
                || groupData.members.any { item -> !item.publicKey.isValidPublicKey() }
            ) {
                return null
            }
            return groupData
        } catch (_: Exception) {
            return null
        }
    }

    suspend fun getVertex(address: String): Response<Vertex?> {
        return enigmaApi.getVertex(address)
    }
}
