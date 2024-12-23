package ro.aenigma.crypto

object StringExtensions {
    fun String?.oneLine(): String? {
        return this?.replace("\n", "")
            ?.replace("\r", "")
            ?.replace(" ", "")
    }
}
