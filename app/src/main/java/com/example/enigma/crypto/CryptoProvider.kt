package com.example.enigma.crypto

class CryptoProvider {

    companion object {
        private external fun encrypt(handle: Long, plaintext: ByteArray) : ByteArray?

        private external fun decrypt(handle: Long, ciphertext: ByteArray) : ByteArray?

        private external fun sign(handle: Long, data: ByteArray) : ByteArray?

        private external fun verify(handle: Long, signature: ByteArray) : Boolean

        private fun throwError(requiredHandleType: String?) : String
        {
            throw IllegalArgumentException("Handle should be an instance of $requiredHandleType")
        }

        fun encrypt(handle : CryptoContextHandle, plaintext: ByteArray) : ByteArray?
        {
            if(handle !is CryptoContextHandle.EncryptionContextHandle)
            {
                throwError(CryptoContextHandle.EncryptionContextHandle::class.qualifiedName)
            }

            return encrypt(handle.handle, plaintext)
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

            context.free()
            return result
        }

        fun verify(handle: CryptoContextHandle, signature: ByteArray) : Boolean
        {
            if(handle !is CryptoContextHandle.SignatureVerificationContextHandle)
            {
                throwError(CryptoContextHandle.SignatureVerificationContextHandle::class.qualifiedName)
            }

            return verify(handle.handle, signature)
        }
    }
}
