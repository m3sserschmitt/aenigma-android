package ro.aenigma.crypto.extensions

import android.util.Base64
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.extensions.Base64Extensions.isValidBase64
import ro.aenigma.crypto.extensions.HashExtensions.getSha256
import ro.aenigma.crypto.extensions.PublicKeyExtensions.isValidPublicKey
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.models.MessageWithMetadata
import ro.aenigma.models.SignedMessageWithMetadata
import ro.aenigma.util.SerializerExtensions.map
import ro.aenigma.util.SerializerExtensions.toJson

object SignatureExtensions {
    @JvmStatic
    fun String?.getDataFromSignature(publicKey: String): ByteArray? {
        if (!publicKey.isValidPublicKey() || !this.isValidBase64()) {
            return null
        }

        val decodedData = Base64.decode(this, Base64.DEFAULT) ?: return null
        val digestSize = CryptoProvider.Companion.getPublicKeySize(publicKey)

        if (decodedData.size < digestSize + 1) {
            return null
        }

        return decodedData.sliceArray(0 until decodedData.size - digestSize)
    }

    @JvmStatic
    fun String?.getStringDataFromSignature(publicKey: String): String? {
        return try {
            this.getDataFromSignature(publicKey)?.toString(Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    fun MessageWithMetadata.sign(signatureService: SignatureService): SignedMessageWithMetadata? {
        val hash = this.toJson()?.getSha256() ?: return null
        val signature = signatureService.sign(hash) ?: return null
        return SignedMessageWithMetadata(
            text = this.text,
            type = this.type,
            senderName = this.senderName,
            groupResourceUrl = this.groupResourceUrl,
            senderGuardAddress = this.senderGuardAddress,
            senderGuardHostname = this.senderGuardHostname,
            senderPublicKey = this.senderPublicKey,
            refId = this.refId,
            actionFor = this.actionFor,
            signature = signature.second
        )
    }

    @JvmStatic
    fun SignedMessageWithMetadata.verify(): Boolean {
        this.senderPublicKey ?: return false
        this.signature ?: return false
        if(!CryptoProvider.verifyEx(this.senderPublicKey, this.signature))
        {
            return false
        }
        val messageWithMetadata: MessageWithMetadata = this.map() ?: return false
        val hash = messageWithMetadata.toJson()?.getSha256() ?: return false
        val data = this.signature.getDataFromSignature(this.senderPublicKey) ?: return false
        return hash.contentEquals(data)
    }
}
