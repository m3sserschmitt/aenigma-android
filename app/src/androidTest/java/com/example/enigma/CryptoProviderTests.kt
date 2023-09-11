package com.example.enigma

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.enigma.crypto.CryptoContext
import com.example.enigma.crypto.CryptoProvider
import com.example.enigma.util.Constants.Companion.PASSPHRASE
import com.example.enigma.util.Constants.Companion.PRIVATE_KEY
import com.example.enigma.util.Constants.Companion.PUBLIC_KEY
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoProviderTests {

    companion object {
        init {
            System.loadLibrary("cryptography-wrapper")
        }
    }

    @Test
    fun shouldEncrypt()
    {
        // Arrange
        val plaintext = "12345678".toByteArray()
        val handle = CryptoContext.Factory.createEncryptionContext(PUBLIC_KEY)

        // Act
        val result = CryptoProvider.encrypt(handle, plaintext)

        // Assert
        assertNotNull(result)
        assertEquals(256 + 12 + 16 + plaintext.size, result!!.size)
    }

    @Test
    fun shouldSign()
    {
        // Arrange
        val plaintext = "12345678".toByteArray()
        val handle = CryptoContext.Factory.createSignatureContext(PRIVATE_KEY, PASSPHRASE)

        // Act
        val result = CryptoProvider.sign(handle, plaintext)

        // Assert
        assertNotNull(result)
        assertEquals(256 + plaintext.size, result!!.size)
    }
}
