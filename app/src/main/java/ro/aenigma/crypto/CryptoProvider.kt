/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

package ro.aenigma.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import ro.aenigma.crypto.extensions.AddressExtensions.isValidAddress
import ro.aenigma.crypto.extensions.Base64Extensions.isValidBase64
import ro.aenigma.crypto.extensions.PublicKeyExtensions.isValidPublicKey
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoProvider {
    init {
        generateMasterKey()
        System.loadLibrary("aenigma-wrapper")
    }

    private const val MASTER_KEY_ALIAS = "AenigmaMasterKey"

    private const val MASTER_KEY_PROVIDER = "AndroidKeyStore"

    private const val MASTER_KEY_BITS_SIZE = 256

    private const val PRIVATE_KEY_ENCRYPTION_CIPHER = "AES/GCM/NoPadding"

    private const val AUTHENTICATION_TAG_BITS_SIZE = 128

    private const val IV_BYTES_SIZE = 12

    private external fun initDecryption(privateKey: ByteArray, passphrase: ByteArray): Boolean

    private external fun initSignature(privateKey: ByteArray, passphrase: ByteArray): Boolean

    private external fun encryptSymmetric(key: ByteArray, plaintext: ByteArray): ByteArray?

    private external fun decryptSymmetric(key: ByteArray, ciphertext: ByteArray): ByteArray?

    private external fun sign(data: ByteArray): ByteArray?

    private external fun verify(publicKey: ByteArray, signedData: ByteArray): Boolean

    private external fun unsealOnion(onion: ByteArray): ByteArray?

    private external fun sealOnion(
        plaintext: ByteArray,
        keys: Array<ByteArray>,
        addresses: Array<ByteArray>
    ): ByteArray?

    private external fun getPKeySize(publicKey: ByteArray): Int

    @JvmStatic
    fun getPublicKeySize(publicKey: String): Int {
        return if (publicKey.isValidPublicKey()) {
            getPKeySize(publicKey.toByteArray())
        } else {
            -1
        }
    }

    @JvmStatic
    fun initDecryptionEx(privateKey: ByteArray, passphrase: ByteArray): Boolean {
        return try {
            initDecryption(privateKey, passphrase)
        } finally {
            privateKey.fill(0)
            passphrase.fill(0)
        }
    }

    @JvmStatic
    fun initDecryptionEx(privateKey: ByteArray): Boolean {
        return initDecryptionEx(privateKey, ByteArray(0))
    }

    @JvmStatic
    fun initSignatureEx(privateKey: ByteArray, passphrase: ByteArray): Boolean {
        return try {
            initSignature(privateKey, passphrase)
        } finally {
            privateKey.fill(0)
            passphrase.fill(0)
        }
    }

    @JvmStatic
    fun initSignatureEx(privateKey: ByteArray): Boolean {
        return initSignatureEx(privateKey, ByteArray(0))
    }

    @JvmStatic
    fun signEx(data: ByteArray): String? {
        return base64Encode(sign(data) ?: return null)
    }

    @JvmStatic
    fun verifyEx(publicKey: String, signedData: String): Boolean {
        if (!publicKey.isValidPublicKey() || !signedData.isValidBase64()) {
            return false
        }
        return verify(publicKey.toByteArray(), base64Decode(signedData) ?: return false)
    }

    @JvmStatic
    fun sealOnionEx(
        plaintext: ByteArray,
        keys: Array<String>,
        addresses: Array<String>
    ): String? {
        if (keys.size != addresses.size || keys.any { item -> !item.isValidPublicKey() }
            || addresses.any { item -> !item.isValidAddress() }) {
            return null
        }
        return base64Encode(
            sealOnion(
                plaintext,
                keys.map { key -> key.toByteArray() }.toTypedArray(),
                addresses.map { addresses -> addresses.toByteArray() }.toTypedArray()
            ) ?: return null
        )
    }

    @JvmStatic
    fun unsealOnionEx(ciphertext: String): ByteArray? {
        if (!ciphertext.isValidBase64()) {
            return null
        }
        return unsealOnion(base64Decode(ciphertext) ?: return null)
    }

    @JvmStatic
    fun encrypt(plaintext: ByteArray, key: ByteArray): ByteArray? {
        return encryptSymmetric(key, plaintext)
    }

    @JvmStatic
    fun encrypt(file: File, outFile: File, key: ByteArray): Boolean {
        var encryptedData: ByteArray? = null
        return try {
            encryptedData = encrypt(file.readBytes(), key) ?: return false
            outFile.outputStream().buffered().use { output -> output.write(encryptedData) }
            true
        } catch (_: Exception) {
            false
        } finally {
            encryptedData?.fill(0)
        }
    }

    @JvmStatic
    fun decrypt(file: File, outFile: File, key: ByteArray): Boolean {
        var decryptedData: ByteArray? = null
        return try {
            decryptedData = decrypt(file.readBytes(), key) ?: return false
            outFile.outputStream().buffered().use { output -> output.write(decryptedData) }
            true
        } catch (_: Exception) {
            false
        } finally {
            decryptedData?.fill(0)
        }
    }

    @JvmStatic
    fun decrypt(ciphertext: ByteArray, key: ByteArray): ByteArray? {
        return decryptSymmetric(key, ciphertext)
    }

    @JvmStatic
    fun generateRandomBytes(size: Int): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(size)
        random.nextBytes(bytes)
        return bytes
    }

    private fun getKeyStore(): KeyStore {
        return KeyStore.getInstance(MASTER_KEY_PROVIDER).apply { load(null) }
    }

    @JvmStatic
    private fun generateMasterKey(): Boolean {
        try {
            if (getKeyStore().containsAlias(MASTER_KEY_ALIAS)) {
                return true
            }
            val keyGen = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                MASTER_KEY_PROVIDER
            )
            val spec = KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(MASTER_KEY_BITS_SIZE)
                .build()
            keyGen.init(spec)
            keyGen.generateKey()
            return true
        } catch (_: Exception) {
            return false
        }
    }

    @JvmStatic
    private fun getMasterKey(): SecretKey {
        return (getKeyStore().getEntry(MASTER_KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    @JvmStatic
    private fun getGCMParametersSpec(ciphertext: ByteArray): GCMParameterSpec {
        return GCMParameterSpec(
            AUTHENTICATION_TAG_BITS_SIZE,
            ciphertext.take(IV_BYTES_SIZE).toByteArray()
        )
    }

    @JvmStatic
    fun masterKeyEncrypt(plaintext: ByteArray): ByteArray? {
        try {
            val cipher = Cipher.getInstance(PRIVATE_KEY_ENCRYPTION_CIPHER)
            cipher.init(Cipher.ENCRYPT_MODE, getMasterKey())
            return cipher.iv + cipher.doFinal(plaintext)
        } catch (_: Exception) {
            return null
        }
    }

    @JvmStatic
    fun masterKeyDecrypt(ciphertext: ByteArray): ByteArray? {
        try {
            val cipher = Cipher.getInstance(PRIVATE_KEY_ENCRYPTION_CIPHER)
            cipher.init(Cipher.DECRYPT_MODE, getMasterKey(), getGCMParametersSpec(ciphertext))
            return cipher.doFinal(ciphertext.copyOfRange(IV_BYTES_SIZE, ciphertext.size))
        } catch (_: Exception) {
            return null
        }
    }

    @JvmStatic
    fun base64Encode(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    @JvmStatic
    fun base64Decode(data: String): ByteArray? {
        return try {
            Base64.decode(data, Base64.DEFAULT)
        } catch (_: Exception) {
            null
        }
    }
}
