package ro.aenigma.data

import ro.aenigma.data.network.EnigmaApi
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
import ro.aenigma.crypto.extensions.SignatureExtensions.getStringDataFromSignature
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.models.GroupData
import ro.aenigma.models.Neighborhood
import ro.aenigma.models.extensions.GroupDataExtensions.withMembers
import ro.aenigma.util.SerializerExtensions.fromJson
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val enigmaApi: EnigmaApi,
    private val signatureService: SignatureService
) {
    suspend fun getServerInfo(): Response<ServerInfo?> {
        return enigmaApi.getServerInfo()
    }

    suspend fun getVertices(): List<Vertex> {
        val response = enigmaApi.getVertices()
        val body = response.body()

        if (response.code() != 200 || body == null) {
            return listOf()
        }

        return body.mapNotNull { vertex -> validateVertex(vertex, false) }
    }

    suspend fun createSharedData(data: String, accessCount: Int = 1): CreatedSharedData? {
        try {
            val signature = signatureService.sign(data.toByteArray())
            signature.signedData ?: return null
            signature.publicKey ?: return null
            val sharedDataCreate =
                SharedDataCreate(signature.publicKey, signature.signedData, accessCount)
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

    private fun decryptGroupData(sharedData: SharedData): GroupData? {
        try {
            val encryptedDataList =
                sharedData.data.getStringDataFromSignature(sharedData.publicKey!!)
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
            return decryptedContent.toString(Charsets.UTF_8).fromJson<GroupData>()
        } catch (_: Exception) {
            return null
        }
    }

    suspend fun getGroupData(tag: String, existentGroups: List<GroupData>): GroupData? {
        try {
            val response = getSharedData(tag) ?: return null
            val groupData = decryptGroupData(response) ?: return null
            val publisherAddress = response.publicKey.getAddressFromPublicKey() ?: return null
            return validateGroupData(groupData, publisherAddress, existentGroups)
        } catch (_: Exception) {
            return null
        }
    }

    private fun validateGroupData(
        groupData: GroupData,
        publisherAddress: String,
        existingGroups: List<GroupData>
    ): GroupData? {
        if (groupData.name == null || groupData.address == null || groupData.members == null
            || groupData.members.isEmpty()
            || groupData.members.any { item -> item.address != item.publicKey.getAddressFromPublicKey() }
            || groupData.admins == null || groupData.admins.isEmpty()
            || groupData.admins.any { item -> !item.isValidAddress() }
        ) {
            return null
        }

        val existingGroupData =
            existingGroups.firstOrNull { item -> item.address == groupData.address }
        val publisherIsAdmin = groupData.admins.contains(publisherAddress)
        val publisherIsNotAdmin = !publisherIsAdmin
        val newGroup = existingGroupData == null
        val existingGroup = !newGroup
        val adminModifiesGroup = !newGroup
                && existingGroupData.admins?.contains(publisherAddress) == true

        when {
            publisherIsNotAdmin && existingGroup -> {
                val eliminatedMember =
                    existingGroupData.members?.toSet()?.subtract(groupData.members)?.singleOrNull()
                return if (existingGroupData.members?.size == groupData.members.size + 1
                    && eliminatedMember?.address == publisherAddress
                ) {
                    groupData.withMembers(groupData.members)
                } else {
                    null
                }
            }

            publisherIsAdmin && (newGroup || adminModifiesGroup) -> return groupData

            else -> return null
        }
    }

    suspend fun getVertex(address: String, isLeaf: Boolean, publicKey: String? = null): Vertex? {
        val response = enigmaApi.getVertex(address)
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
