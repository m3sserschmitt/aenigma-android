package ro.aenigma.util

class HexConverter {
    companion object {

        @JvmStatic
        fun toHex(address: ByteArray): String {
            return address.joinToString("") { byte ->
                "%02x".format(byte).lowercase()
            }
        }
    }
}
