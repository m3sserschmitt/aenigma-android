package ro.aenigma.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import ro.aenigma.crypto.extensions.AddressExtensions.isValidAddress
import ro.aenigma.crypto.extensions.Base64Extensions.isValidBase64
import ro.aenigma.crypto.extensions.PublicKeyExtensions.isValidPrivateKey
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

    private external fun initDecryption(privateKey: String, passphrase: String): Boolean

    private external fun initSignature(privateKey: String, passphrase: String): Boolean

    private external fun encrypt(publicKey: String, plaintext: ByteArray): ByteArray?

    private external fun encryptSymmetric(key: ByteArray, plaintext: ByteArray): ByteArray?

    private external fun decrypt(ciphertext: ByteArray): ByteArray?

    private external fun decryptSymmetric(key: ByteArray, ciphertext: ByteArray): ByteArray?

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
    fun getPublicKeySize(publicKey: String): Int {
        return if (publicKey.isValidPublicKey()) {
            getPKeySize(publicKey)
        } else {
            -1
        }
    }

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
    fun encryptEx(publicKey: String, plaintext: ByteArray): String? {
        if (!publicKey.isValidPublicKey()) {
            return null
        }
        val encryptedData = encrypt(publicKey, plaintext) ?: return null
        return base64Encode(encryptedData)
    }

    @JvmStatic
    fun decryptEx(ciphertext: String): ByteArray? {
        if (!ciphertext.isValidBase64()) {
            return null
        }
        return decrypt(base64Decode(ciphertext) ?: return null)
    }

    @JvmStatic
    fun signEx(data: ByteArray): String? {
        val signature = sign(data)
        return base64Encode(signature ?: return null)
    }

    @JvmStatic
    fun verifyEx(publicKey: String, signedData: String): Boolean {
        if (!publicKey.isValidPublicKey() || !signedData.isValidBase64()) {
            return false
        }
        return verify(publicKey, base64Decode(signedData) ?: return false)
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
        return base64Encode(sealOnion(plaintext, keys, addresses) ?: return null)
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
    fun encrypt(file: File, key: ByteArray): File? {
        var encryptedData: ByteArray? = null
        return try {
            encryptedData = encrypt(file.readBytes(), key) ?: return null
            val outFile = File(file.parentFile, "${file.name}_encrypted")
            outFile.outputStream().use { output ->
                output.write(encryptedData)
            }
            outFile
        } catch (_: Exception) {
            null
        } finally {
            encryptedData?.fill(0)
        }
    }

    @JvmStatic
    fun decrypt(file: File, key: ByteArray): File? {
        return try {
            val decryptedData = decrypt(file.readBytes(), key) ?: return null
            val outFile = File(file.parentFile, "${file.name}_decrypted")
            outFile.outputStream().use { output ->
                output.write(decryptedData)
            }
            outFile
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    fun decrypt(ciphertext: ByteArray, key: ByteArray, ): ByteArray? {
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
    fun masterKeyEncryptEx(plaintext: ByteArray): String? {
        return base64Encode(masterKeyEncrypt(plaintext) ?: return null)
    }

    @JvmStatic
    fun masterKeyDecryptEx(ciphertext: String): ByteArray? {
        return masterKeyDecrypt(base64Decode(ciphertext) ?: return null)
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
