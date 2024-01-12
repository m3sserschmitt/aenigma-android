package com.example.enigma.crypto

import android.content.Context
import android.util.Base64
import com.example.enigma.models.Message
import com.example.enigma.onion.OnionParser
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnionParsingService @Inject constructor(@ApplicationContext context: Context) {

    private val parser: OnionParser?

    init {
        val key = KeysManager.readPrivateKey(context)
        parser = if(key != null) {
            val cryptoContext = CryptoContext.Factory.createDecryptionContext(key, "")
            OnionParser(cryptoContext)
        }else {
            null
        }
    }

    fun parse(ciphertext: String): Message?
    {
        val decodedMessage = Base64.decode(ciphertext, Base64.DEFAULT)
        return parser?.parse(decodedMessage)
    }
}
