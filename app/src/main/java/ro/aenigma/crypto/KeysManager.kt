package ro.aenigma.crypto

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeysManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val lock = Any()

    companion object {

        private const val PRIVATE_KEY_FILE = "private-key.locked"

        private const val PUBLIC_KEY_FILE = "public-key.pem"

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
            CryptoProvider.generateMasterKey()
            val keyPairDto = KeysHelper.keyPairToPEM()
            val encryptedPrivateKey =
                CryptoProvider.masterKeyEncrypt(
                    keyPairDto.privateKey?.toByteArray() ?: return false
                ) ?: return false
            writeKey(getPrivateKeyFile(), encryptedPrivateKey)
            writeKey(getPublicKeyFile(), keyPairDto.publicKey?.toByteArray() ?: return false)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun readPrivateKey(): String? {
        return try {
            String(CryptoProvider.masterKeyDecrypt(readKey(getPrivateKeyFile()) ?: return null) ?: return null)
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
