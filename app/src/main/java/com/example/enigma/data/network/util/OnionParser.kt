package com.example.enigma.data.network.util

import com.example.enigma.crypto.CryptoContextHandle
import com.example.enigma.crypto.CryptoProvider
import com.example.enigma.models.Message
import com.example.enigma.util.AddressHexConverter
import com.example.enigma.util.Constants.Companion.ADDRESS_SIZE

class OnionParser constructor(private val cryptoContextHandle: CryptoContextHandle) {

    fun parse(onion: ByteArray): Message?
    {
        val size = decodeSize(onion)

        if(onion.size - 2 != size)
        {
            return null
        }

        try {
            val encryptedData = onion.sliceArray(2 until onion.size)
            val decryptedData = CryptoProvider.decrypt(cryptoContextHandle, encryptedData) ?: return null

            val address = AddressHexConverter.toHex(decryptedData.sliceArray(0 until ADDRESS_SIZE))
            val content = String(decryptedData.sliceArray(ADDRESS_SIZE until decryptedData.size))

            return Message(address, content, true)

        } catch (e: Exception){
            return null
        }
    }

    companion object {

        @JvmStatic
        fun decodeSize(onion: ByteArray): Int
        {
            return onion[0].times(256).plus(onion[1])
        }
    }
}
