package com.example.enigma.util

class HexConverter {

    companion object {

        @JvmStatic
        fun toHex(address: ByteArray): String {
            return address.joinToString("") { byte ->
                "%02x".format(byte).lowercase()
            }
        }

        @JvmStatic
        fun fromHex(hexString: String): ByteArray {
            if (hexString.length % 2 != 0) {
                throw IllegalArgumentException("Invalid hexadecimal string.")
            }

            val byteCount = hexString.length / 2
            val byteArray = ByteArray(byteCount)

            for (i in 0 until byteCount) {
                val byteString = hexString.substring(i * 2, i * 2 + 2)
                byteArray[i] = byteString.toInt(16).toByte()
            }

            return byteArray
        }
    }
}
