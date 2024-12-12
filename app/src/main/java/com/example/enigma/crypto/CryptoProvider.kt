package com.example.enigma.crypto

import android.util.Base64
import com.example.enigma.crypto.AddressExtensions.isValidAddress
import com.example.enigma.crypto.Base64Extensions.isValidBase64
import com.example.enigma.crypto.PublicKeyExtensions.isValidPrivateKey
import com.example.enigma.crypto.PublicKeyExtensions.isValidPublicKey

class CryptoProvider {
    companion object {

        init {
            System.loadLibrary("libaenigma-wrapper")
        }

        private external fun initDecryption(privateKey: String, passphrase: String): Boolean

        private external fun initSignature(privateKey: String, passphrase: String): Boolean

        private external fun encrypt(publicKey: String, plaintext: ByteArray): ByteArray?

        private external fun decrypt(ciphertext: ByteArray): ByteArray?

        private external fun sign(data: ByteArray): ByteArray?

        private external fun verify(publicKey: String, signedData: ByteArray): Boolean

        private external fun unsealOnion(onion: ByteArray): ByteArray?

        private external fun sealOnion(
            plaintext: ByteArray,
            keys: Array<String>,
            addresses: Array<String>
        ): ByteArray?

        private external fun getPKeySize(publicKey: String): Int

        @JvmStatic
        fun initDecryptionEx(privateKey: String, passphrase: String): Boolean {
            if (!privateKey.isValidPrivateKey()) {
                return false
            }
            return initDecryption(privateKey, passphrase)
        }

        @JvmStatic
        fun initDecryptionEx(privateKey: String): Boolean {
            return initDecryptionEx(privateKey, "")
        }

        @JvmStatic
        fun initSignatureEx(privateKey: String, passphrase: String): Boolean {
            if (!privateKey.isValidPrivateKey()) {
                return false
            }
            return initSignature(privateKey, passphrase)
        }

        @JvmStatic
        fun initSignatureEx(privateKey: String): Boolean {
            return initSignatureEx(privateKey, "")
        }

        @JvmStatic
        fun encryptEx(publicKey: String, plaintext: ByteArray): ByteArray? {
            if (!publicKey.isValidPublicKey()) {
                return null
            }
            return encrypt(publicKey, plaintext)
        }

        @JvmStatic
        fun decryptEx(ciphertext: ByteArray): ByteArray? {
            return decrypt(ciphertext)
        }

        @JvmStatic
        fun signEx(data: ByteArray): String? {
            val signature = sign(data)
            return if (signature != null)
                Base64.encodeToString(signature, Base64.DEFAULT) else null
        }

        @JvmStatic
        fun verifyEx(publicKey: String, signedData: String): Boolean {
            if (!publicKey.isValidPublicKey() || !signedData.isValidBase64()) {
                return false
            }
            val decodedSignedData = Base64.decode(signedData, Base64.DEFAULT) ?: return false
            return verify(publicKey, decodedSignedData)
        }

        @JvmStatic
        fun sealOnionEx(
            plaintext: ByteArray,
            keys: Array<String>,
            addresses: Array<String>
        ): String? {
            if (keys.size != addresses.size || keys.any { item -> !item.isValidPublicKey() } || addresses.any { item -> !item.isValidAddress() }) {
                return null
            }
            val data = sealOnion(plaintext, keys, addresses) ?: return null
            return Base64.encodeToString(data, Base64.DEFAULT)
        }

        @JvmStatic
        fun unsealOnionEx(ciphertext: String): ByteArray? {
            if (!ciphertext.isValidBase64()) {
                return null
            }
            val decodedMessage = Base64.decode(ciphertext, Base64.DEFAULT) ?: return null
            return unsealOnion(decodedMessage)
        }

        @JvmStatic
        fun getDataFromSignature(signedData: String, publicKey: String): ByteArray? {
            if (!publicKey.isValidPublicKey() || !signedData.isValidBase64()) {
                return null
            }

            val decodedData = Base64.decode(signedData, Base64.DEFAULT) ?: return null
            val digestSize = getPKeySize(publicKey) / 8

            if (decodedData.size < digestSize + 1) {
                return null
            }

            return decodedData.sliceArray(0 until decodedData.size - digestSize)
        }
    }
}
