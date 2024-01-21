package com.example.enigma.crypto

import android.content.Context
import com.example.enigma.models.Message
import com.example.enigma.util.Constants
import com.example.enigma.util.HexConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnionParsingService @Inject constructor(@ApplicationContext context: Context) {

    private val cryptoContextHandle: CryptoContextHandle?

    init {
        val key = KeysManager.readPrivateKey(context)
        cryptoContextHandle = if(key != null) {
            CryptoContext.Factory.createDecryptionContext(key, "")
        }else {
            null
        }
    }

    val isReady: Boolean get() = cryptoContextHandle != null

    fun parse(ciphertext: String): Message?
    {
        if(cryptoContextHandle == null) return null

        val decryptedData = CryptoProvider.parseOnion(cryptoContextHandle, ciphertext) ?: return null

        val address = HexConverter.toHex(decryptedData.sliceArray(0 until Constants.ADDRESS_SIZE))
        val content = String(decryptedData.sliceArray(Constants.ADDRESS_SIZE until decryptedData.size))

        return Message(address, content, true)
    }
}
