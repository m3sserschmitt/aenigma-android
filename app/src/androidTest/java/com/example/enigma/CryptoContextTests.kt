package com.example.enigma

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.enigma.crypto.CryptoContext
import com.example.enigma.crypto.CryptoContextHandle
import com.example.enigma.util.Constants.Companion.PASSPHRASE
import com.example.enigma.util.Constants.Companion.PRIVATE_KEY
import com.example.enigma.util.Constants.Companion.PUBLIC_KEY

import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class CryptoContextTests {

    companion object {
        init {
            System.loadLibrary("cryptography-wrapper")
        }
    }

    @Test
    fun shouldSuccessfullyCreateEncryptionContext() {

        // Arrange

        // Act
        val handle = CryptoContext.Factory.createEncryptionContext(PUBLIC_KEY)

        // Assert
        assertNotEquals(-1, handle.handle)
        assertEquals(true, handle is CryptoContextHandle.EncryptionContextHandle)
    }

    @Test
    fun shouldSuccessfullyCreateDecryptionContext()
    {
        // Arrange

        // Act
        val handle = CryptoContext.Factory.createDecryptionContext(PRIVATE_KEY, PASSPHRASE)

        // Assert
        assertNotEquals(-1, handle.handle)
        assertEquals(true, handle is CryptoContextHandle.DecryptionContextHandle)
    }

    @Test
    fun shouldSuccessfullyCreateSignatureContext()
    {
        // Arrange

        // Act
        val handle = CryptoContext.Factory.createSignatureContext(PRIVATE_KEY, PASSPHRASE)

        // Assert
        assertNotEquals(-1, handle.handle)
        assertEquals(true, handle is CryptoContextHandle.SignatureContextHandle)
    }

    @Test
    fun shouldSuccessfullyCreateSignatureVerificationContext()
    {
        // Arrange

        // Act
        val handle = CryptoContext.Factory.createSignatureVerificationContext(PRIVATE_KEY)

        // Assert
        assertNotEquals(-1, handle.handle)
        assertEquals(true, handle is CryptoContextHandle.SignatureVerificationContextHandle)
    }

    @Test
    fun shouldSuccessfullyFreeContext()
    {
        // Arrange
        val handle = CryptoContext.Factory.createEncryptionContext(PUBLIC_KEY)

        // Act
        val disposed = handle.dispose()

        assertTrue(disposed)
    }
}
