package ro.aenigma.crypto

import ro.aenigma.util.Constants.Companion.KEY_SIZE_BITS
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import ro.aenigma.models.KeyPairDto
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.io.StringWriter
import java.security.Key

class KeysHelper {

    companion object {

        private const val KEYS_ALGORITHM = "RSA"

        @JvmStatic
        fun generateKeyPair(): KeyPair? {
            return try {
                val keyPairGenerator = KeyPairGenerator.getInstance(KEYS_ALGORITHM)
                keyPairGenerator.initialize(KEY_SIZE_BITS)
                keyPairGenerator.generateKeyPair()
            } catch (_: Exception) {
                null
            }
        }

        @JvmStatic
        private fun toPEM(key: Key): String {
            val stringWriter = StringWriter()
            val jcaWriter = JcaPEMWriter(stringWriter)
            jcaWriter.writeObject(key)
            jcaWriter.close()
            return stringWriter.toString()
        }

        @JvmStatic
        fun keyPairToPEM(): KeyPairDto {
            return try {
                val keyPair = generateKeyPair()
                KeyPairDto(
                    publicKey = toPEM(keyPair?.public ?: throw Exception()),
                    privateKey = toPEM(keyPair.private ?: throw Exception())
                )
            } catch (_: Exception) {
                KeyPairDto(publicKey = null, privateKey = null)
            }
        }
    }
}
