package com.example.enigma.crypto

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class KeysManager {

    companion object {

        private const val PRIVATE_KEY_FILE = "private-key"

        private const val PUBLIC_KEY_FILE = "public-key"

        @JvmStatic
        fun keysExists(context: Context): Boolean
        {
            return File(context.filesDir, PRIVATE_KEY_FILE).exists()
        }

        @JvmStatic
        fun generateKeys(context: Context): Boolean
        {
            val keys = KeyGenerator.keyPairToPEM()

            val publicKeyFile = File(context.filesDir, PUBLIC_KEY_FILE)
            val privateKeyFile = File(context.filesDir, PRIVATE_KEY_FILE)

            return try {
                val publicOutputStream = FileOutputStream(publicKeyFile)
                val privateOutputStream = FileOutputStream(privateKeyFile)

                // TODO: implement encryption for private key
                publicOutputStream.write(keys.first.toByteArray())
                privateOutputStream.write(keys.second.toByteArray())

                publicOutputStream.close()
                privateOutputStream.close()

                true
            } catch (e: Exception) {
                false
            }
        }

        @JvmStatic
        private fun readKey(context: Context, key: String): ByteArray?
        {
            return try {
                val privateKeyFile = File(context.filesDir, key)
                val privateInputStream = FileInputStream(privateKeyFile)

                privateInputStream.readBytes()
            } catch (ex: Exception) {
                null
            }
        }

        @JvmStatic
        fun readPrivateKey(context: Context): String?
        {
            // TODO: implement decryption for private key

            val key = readKey(context, PRIVATE_KEY_FILE) ?: return null

            return String(key)
        }

        @JvmStatic
        fun readPublicKey(context: Context): String?
        {
            val key = readKey(context, PUBLIC_KEY_FILE) ?: return null

            return String(key)
        }

        @JvmStatic
        fun generateKeyIfNotExistent(context: Context): Boolean
        {
            if(!keysExists(context))
            {
                return generateKeys(context)
            }

            return true
        }
    }
}
