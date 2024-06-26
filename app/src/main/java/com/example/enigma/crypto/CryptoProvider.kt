package com.example.enigma.crypto

import android.util.Base64

class CryptoProvider {

    companion object {

        private external fun encrypt(handle: Long, plaintext: ByteArray) : ByteArray?

        private external fun decrypt(handle: Long, ciphertext: ByteArray) : ByteArray?

        private external fun sign(handle: Long, data: ByteArray) : ByteArray?

        private external fun verify(handle: Long, signature: ByteArray) : Boolean

        private external fun unsealOnion(handle: Long, onion: ByteArray): ByteArray?

        private external fun sealOnion(plaintext: ByteArray, keys: Array<String>, addresses: Array<String>): ByteArray?

        private external fun calculateEnvelopeSize(currentSize: Int): Int

        private external fun getDefaultPKeySize(): Int

        @JvmStatic
        private fun throwError(requiredHandleType: String?)
        {
            throw IllegalArgumentException("Handle should be an instance of $requiredHandleType.")
        }

        @JvmStatic
        fun envelopeSizeExceeded(currentSize: Int): Boolean
        {
            return currentSize > UShort.MAX_VALUE.toInt()
        }

        @JvmStatic
        fun canAddLayer(currentSize: Int): Boolean
        {
            return calculateEnvelopeSize(currentSize) < UShort.MAX_VALUE.toInt()
        }

        @JvmStatic
        fun encrypt(handle : CryptoContextHandle, plaintext: ByteArray) : ByteArray?
        {
            if(handle !is CryptoContextHandle.EncryptionContextHandle)
            {
                throwError(CryptoContextHandle.EncryptionContextHandle::class.qualifiedName)
            }

            return encrypt(handle.handle, plaintext)
        }

        @JvmStatic
        fun encrypt(key: String, plaintext: ByteArray): ByteArray?
        {
            val context = CryptoContext.Factory.createEncryptionContext(key)

            val result = encrypt(context, plaintext)

            context.dispose()
            return result
        }

        @JvmStatic
        fun decrypt(handle: CryptoContextHandle, ciphertext: ByteArray) : ByteArray?
        {
            if(handle !is CryptoContextHandle.DecryptionContextHandle)
            {
                throwError(CryptoContextHandle.DecryptionContextHandle::class.qualifiedName)
            }

            return decrypt(handle.handle, ciphertext)
        }

        @JvmStatic
        fun sign(handle: CryptoContextHandle, data: ByteArray) : ByteArray?
        {
            if(handle !is CryptoContextHandle.SignatureContextHandle)
            {
                throwError(CryptoContextHandle.SignatureContextHandle::class.qualifiedName)
            }

            return sign(handle.handle, data)
        }

        @JvmStatic
        fun sign(key: String, passphrase: String, data: ByteArray) : ByteArray?
        {
            val context = CryptoContext.Factory.createSignatureContext(key, passphrase)

            val result = sign(context, data)

            context.dispose()
            return result
        }

        @JvmStatic
        fun verify(handle: CryptoContextHandle, signature: ByteArray) : Boolean
        {
            if(handle !is CryptoContextHandle.SignatureVerificationContextHandle)
            {
                throwError(CryptoContextHandle.SignatureVerificationContextHandle::class.qualifiedName)
            }

            return verify(handle.handle, signature)
        }

        @JvmStatic
        fun buildOnion(plaintext: ByteArray, keys: Array<String>, addresses: Array<String>): String?
        {
            if(keys.size != addresses.size)
            {
                throw IllegalArgumentException("Number of keys should be equal to number of addresses")
            }

            val data = sealOnion(plaintext, keys, addresses)

            return if (data != null) String(Base64.encode(data, Base64.DEFAULT)) else null
        }

        @JvmStatic
        fun parseOnion(handle: CryptoContextHandle, ciphertext: String): ByteArray? {
            if (handle !is CryptoContextHandle.DecryptionContextHandle) {
                throwError(CryptoContextHandle.SignatureVerificationContextHandle::class.qualifiedName)
            }

            val decodedMessage = Base64.decode(ciphertext, Base64.DEFAULT) ?: return null

            return unsealOnion(handle.handle, decodedMessage)
        }

        @JvmStatic
        fun getDefaultPublicKeySize(): Int
        {
            return getDefaultPKeySize()
        }

        @JvmStatic
        fun getDataFromSignature(signedData: ByteArray?): ByteArray?
        {
            if(signedData == null)
            {
                return null
            }

            val digestSize = getDefaultPublicKeySize() / 8

            if(signedData.size < digestSize + 1)
            {
                return null
            }

            return signedData.sliceArray(0 until signedData.size - digestSize)
        }
    }
}
