package com.example.enigma.crypto

class CryptoProvider {
    companion object
    {
        external fun createEncryptionContext(key : String) : Long

        external fun createDecryptionContext(key : String, passphrase : String) : Long

        external fun createSignatureContext(key: String, passphrase: String) : Long

        external fun createSignatureVerificationContext(key: String): Long

        external fun freeHandle(i : Long)
    }
}
