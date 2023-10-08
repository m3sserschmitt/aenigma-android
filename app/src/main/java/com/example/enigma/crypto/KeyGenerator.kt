package com.example.enigma.crypto

import com.example.enigma.util.Constants.Companion.KEY_SIZE
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.io.StringWriter

class KeyGenerator {

    companion object
    {
        @JvmStatic
        fun generateKeyPair(): KeyPair {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(KEY_SIZE)
            return keyPairGenerator.generateKeyPair()
        }

        @JvmStatic
        fun keyPairToPEM(): Pair<String, String> {
            val keyPair = generateKeyPair()
            val publicKey = keyPair.public
            val privateKey = keyPair.private

            val publicStringWriter = StringWriter()
            val publicPEMWriter = JcaPEMWriter(publicStringWriter)
            publicPEMWriter.writeObject(publicKey)
            publicPEMWriter.close()

            val privateStringWriter = StringWriter()
            val privatePEMWriter = JcaPEMWriter(privateStringWriter)
            privatePEMWriter.writeObject(privateKey)
            privatePEMWriter.close()

            return Pair(publicStringWriter.toString(), privateStringWriter.toString())
        }
    }
}
