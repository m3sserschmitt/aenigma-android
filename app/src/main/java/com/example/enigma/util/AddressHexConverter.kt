package com.example.enigma.util

class AddressHexConverter {

    companion object {

        fun toHex(address: ByteArray): String {
            return address.joinToString("") { byte ->
                "%02x".format(byte).lowercase()
            }
        }
    }
}
