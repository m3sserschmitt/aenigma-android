package com.example.enigma.crypto

sealed class CryptoContextHandle(desc: Long) {

    companion object {
        private external fun freeContext(i : Long) : Boolean
    }

    private var disposed = false

    val handle : Long = desc

    fun free() : Boolean
    {
        if(!disposed)
        {
            disposed = freeContext(handle)
        }

        return disposed
    }

    class EncryptionContextHandle(handle : Long) : CryptoContextHandle(handle)

    class DecryptionContextHandle(handle : Long) : CryptoContextHandle(handle)

    class SignatureContextHandle(handle: Long) : CryptoContextHandle(handle)

    class SignatureVerificationContextHandle(handle: Long) : CryptoContextHandle(handle)
}
