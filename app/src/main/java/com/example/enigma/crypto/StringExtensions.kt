package com.example.enigma.crypto

object StringExtensions {
    fun String?.oneLine(): String? {
        return this?.replace("\n", "")
            ?.replace("\r", "")
            ?.replace(" ", "")
    }
}
