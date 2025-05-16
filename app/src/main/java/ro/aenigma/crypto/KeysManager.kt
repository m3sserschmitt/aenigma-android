package ro.aenigma.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeysManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val lock = Any()

    companion object {

        private const val PRIVATE_KEY_FILE = "private-key.locked"

        private const val PUBLIC_KEY_FILE = "public-key.pem"

        private const val MASTER_KEY_ALIAS = "AenigmaMasterKey"

        private const val MASTER_KEY_PROVIDER = "AndroidKeyStore"

        private const val MASTER_KEY_BITS_SIZE = 256

        private const val PRIVATE_KEY_ENCRYPTION_CIPHER = "AES/GCM/NoPadding"

        private const val AUTHENTICATION_TAG_BITS_SIZE = 128

        private const val IV_BYTES_SIZE = 12

        @JvmStatic
        private fun generateMasterKey(): Boolean {
            try {
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
            val keyStore = KeyStore.getInstance(MASTER_KEY_PROVIDER).apply { load(null) }
            return (keyStore.getEntry(MASTER_KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        }

        @JvmStatic
        private fun getGCMParametersSpec(ciphertext: ByteArray): GCMParameterSpec {
            return GCMParameterSpec(
                AUTHENTICATION_TAG_BITS_SIZE,
                ciphertext.take(IV_BYTES_SIZE).toByteArray()
            )
        }

        @JvmStatic
        private fun encryptData(plaintext: String): ByteArray? {
            try {
                val cipher = Cipher.getInstance(PRIVATE_KEY_ENCRYPTION_CIPHER)
                cipher.init(Cipher.ENCRYPT_MODE, getMasterKey())
                return cipher.iv + cipher.doFinal(plaintext.toByteArray())
            } catch (_: Exception) {
                return null
            }
        }

        @JvmStatic
        private fun decryptData(ciphertext: ByteArray): String? {
            try {
                val cipher = Cipher.getInstance(PRIVATE_KEY_ENCRYPTION_CIPHER)
                cipher.init(Cipher.DECRYPT_MODE, getMasterKey(), getGCMParametersSpec(ciphertext))
                val plaintext =
                    cipher.doFinal(ciphertext.copyOfRange(IV_BYTES_SIZE, ciphertext.size))
                return String(plaintext)
            } catch (_: Exception) {
                return null
            }
        }

        @JvmStatic
        private fun writeKey(file: File, data: ByteArray) {
            val outStream = FileOutputStream(file, false)
            outStream.write(data)
            outStream.close()
        }

        @JvmStatic
        private fun readKey(file: File): ByteArray? {
            return try {
                val stream = FileInputStream(file)
                val data = stream.readBytes()
                stream.close()
                data
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun getPrivateKeyFile(): File {
        return File(context.filesDir, PRIVATE_KEY_FILE)
    }

    private fun getPublicKeyFile(): File {
        return File(context.filesDir, PUBLIC_KEY_FILE)
    }

    private fun keysExists(): Boolean {
        return try {
            getPrivateKeyFile().exists() && getPublicKeyFile().exists()
        } catch (_: Exception) {
            false
        }
    }

    private fun generateKeys(): Boolean {
        return try {
            generateMasterKey()
            val keyPairDto = KeysHelper.keyPairToPEM()
            val encryptedPrivateKey =
                encryptData(keyPairDto.privateKey ?: return false) ?: return false
            writeKey(getPrivateKeyFile(), encryptedPrivateKey)
            writeKey(getPublicKeyFile(), keyPairDto.publicKey?.toByteArray() ?: return false)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun readPrivateKey(): String? {
        return try {
            decryptData(readKey(getPrivateKeyFile()) ?: return null)
        } catch (_: Exception) {
            null
        }
    }

    fun readPublicKey(): String? {
        return try {
            String(readKey(getPublicKeyFile()) ?: return null)
        } catch (_: Exception) {
            null
        }
    }

    fun generateKeyIfNotExistent(): Boolean {
        synchronized(lock) {
            return if (!keysExists()) {
                generateKeys()
            } else {
                true
            }
        }
    }
}
