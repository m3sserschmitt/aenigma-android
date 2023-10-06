package com.example.enigma.util

import android.util.Base64
import java.security.MessageDigest

class AddressHelper {

    companion object {

        @JvmStatic
        fun getAddressFromPublicKey(publicKey: String): ByteArray {
            val lines = publicKey.split('\n').filter { it.isNotEmpty() }.toTypedArray()

            val base64ContentBuilder = StringBuilder()
            for (i in 1 until lines.size - 1) {
                base64ContentBuilder.append(lines[i].trim())
            }

            return MessageDigest.getInstance("SHA-256")
                .digest(Base64.decode(base64ContentBuilder.toString(), Base64.DEFAULT))
        }

        @JvmStatic
        fun getHexAddressFromPublicKey(publicKey: String): String {
            val hash = getAddressFromPublicKey(publicKey)
            return hash.joinToString("") { "%02x".format(it) }
        }
    }
}
