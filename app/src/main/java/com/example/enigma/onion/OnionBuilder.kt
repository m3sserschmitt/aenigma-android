package com.example.enigma.onion

import android.util.Base64
import com.example.enigma.crypto.CryptoProvider
import com.example.enigma.onion.contracts.IEncryptMessage
import com.example.enigma.onion.contracts.IOnionBuilder
import com.example.enigma.onion.contracts.ISetMessageNextAddress
import com.example.enigma.util.Constants.Companion.ADDRESS_SIZE
import com.example.enigma.util.HexConverter

class OnionBuilder private constructor() {

    private class Impl constructor(private var onion: ByteArray)
        : ISetMessageNextAddress,
        IEncryptMessage,
        IOnionBuilder {

        override fun build(): ByteArray {
            return onion
        }

        override fun buildEncode(): String {
            return String(Base64.encode(onion, Base64.DEFAULT))
        }

        override fun addPeel(): ISetMessageNextAddress {
            return this
        }

        override fun seal(key: String): IOnionBuilder {

            if (!checkSize(onion.size + 2)) {
                throw IllegalArgumentException("Maximum size for content exceeded.")
            }

            val ciphertext = CryptoProvider.encrypt(key, onion)
                ?: throw Exception("Message encryption failed.")

            onion = encodeSize(ciphertext.size.toUShort()).plus(ciphertext)

            return this
        }

        override fun setAddress(address: String): IEncryptMessage {
            if (address.length != ADDRESS_SIZE * 2) {
                throw IllegalArgumentException(
                    "Destination address length should be exactly $ADDRESS_SIZE bytes long.")
            }

            val byteAddress = HexConverter.fromHex(address)

            onion = byteAddress.plus(onion)

            return this
        }

        private fun encodeSize(size: UShort): ByteArray {

            val buffer = ByteArray(2)
            buffer[0] = (size.toInt() / 256).toByte()
            buffer[1] = (size.toInt() % 256).toByte()

            return buffer
        }

        private fun checkSize(currentSize: Int): Boolean {
            return !CryptoProvider.envelopeSizeExceeded(currentSize)
                    && CryptoProvider.canAddLayer(currentSize)
        }
    }

    companion object {
        fun create(content: ByteArray): ISetMessageNextAddress {
            return Impl(content)
        }
    }
}
