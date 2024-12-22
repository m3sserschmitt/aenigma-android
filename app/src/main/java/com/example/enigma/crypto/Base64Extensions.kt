package com.example.enigma.crypto

import android.util.Base64

object Base64Extensions {
    fun String?.isValidBase64(): Boolean {
        return !this.isNullOrBlank() && try {
            Base64.decode(this, Base64.DEFAULT)
            true
        } catch (e: Exception) {
            false
        }
    }
}
