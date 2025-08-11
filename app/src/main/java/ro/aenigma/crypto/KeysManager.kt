package ro.aenigma.crypto

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ro.aenigma.util.ContextExtensions.getPrivateKeyFile
import ro.aenigma.util.ContextExtensions.getPublicKeyFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeysManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    companion object {

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

    init {
        generateKeyIfNotExistent()
    }

    private fun keysExists(): Boolean {
        return try {
            context.getPrivateKeyFile().exists() && context.getPublicKeyFile().exists()
        } catch (_: Exception) {
            false
        }
    }

    private fun generateKeys(): Boolean {
        return try {
            val keyPairDto = KeysHelper.keyPairToPEM()
            val encryptedPrivateKey =
                CryptoProvider.masterKeyEncrypt(
                    keyPairDto.privateKey?.toByteArray() ?: return false
                ) ?: return false
            writeKey(context.getPrivateKeyFile(), encryptedPrivateKey)
            writeKey(context.getPublicKeyFile(), keyPairDto.publicKey?.toByteArray() ?: return false)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun readPrivateKey(): String? {
        return try {
            String(
                CryptoProvider.masterKeyDecrypt(readKey(context.getPrivateKeyFile()) ?: return null)
                    ?: return null
            )
        } catch (_: Exception) {
            null
        }
    }

    fun readPublicKey(): String? {
        return try {
            String(readKey(context.getPublicKeyFile()) ?: return null)
        } catch (_: Exception) {
            null
        }
    }

    private fun generateKeyIfNotExistent(): Boolean {
        return if (!keysExists()) {
            generateKeys()
        } else {
            true
        }
    }
}
