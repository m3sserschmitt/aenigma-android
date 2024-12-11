package com.example.enigma.crypto

import java.util.Base64

object Base64Extensions {
    fun String?.isValidBase64(): Boolean {
        return !this.isNullOrBlank() && try {
            Base64.getDecoder().decode(this)
            true
        } catch (e: Exception) {
            false
        }
    }
}
