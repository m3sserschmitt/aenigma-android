package com.example.enigma

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.enigma.onion.OnionBuilder
import com.example.enigma.util.Constants.Companion.HEX_ADDRESS
import com.example.enigma.util.Constants.Companion.PUBLIC_KEY
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnionBuilderTests {

    companion object {
        init {
            System.loadLibrary("cryptography-wrapper")
        }
    }

    @Test
    fun shouldCreateOnion() {
        // Arrange
        val content = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)

        // Act
        val onion = OnionBuilder.create(content)
            .setAddress(HEX_ADDRESS)
            .seal(PUBLIC_KEY)
            .build()

        // Assert
        assertEquals(326, onion.size)
        assertEquals(1, onion[0].toInt())
        assertEquals(68, onion[1].toInt())
    }
}
