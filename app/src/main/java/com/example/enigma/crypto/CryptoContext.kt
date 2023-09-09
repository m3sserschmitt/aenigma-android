package com.example.enigma.crypto

class CryptoContext {
    companion object
    {
        private external fun createEncryptionContext(key : String) : Long

        private external fun createDecryptionContext(key : String, passphrase : String) : Long

        private external fun createSignatureContext(key: String, passphrase: String) : Long

        private external fun createSignatureVerificationContext(key: String): Long
    }

    class Factory {
        companion object {
            fun createEncryptionContext(key: String) : CryptoContextHandle
            {
                return CryptoContextHandle.EncryptionContextHandle(
                    CryptoContext.createEncryptionContext(key))
            }

            @JvmStatic
            fun createDecryptionContext(key : String, passphrase : String) : CryptoContextHandle
            {
                return CryptoContextHandle.DecryptionContextHandle(
                    CryptoContext.createDecryptionContext(key, passphrase))
            }

            fun createSignatureContext(key: String, passphrase: String) : CryptoContextHandle
            {
                return CryptoContextHandle.SignatureContextHandle(
                    CryptoContext.createSignatureContext(key, passphrase))
            }

            fun createSignatureVerificationContext(key: String): CryptoContextHandle
            {
                return CryptoContextHandle.SignatureVerificationContextHandle(
                    CryptoContext.createSignatureVerificationContext(key))
            }
        }
    }
}
