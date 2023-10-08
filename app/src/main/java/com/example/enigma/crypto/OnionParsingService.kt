package com.example.enigma.crypto

import android.util.Base64
import com.example.enigma.data.Repository
import com.example.enigma.models.Message
import com.example.enigma.onion.OnionParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.bouncycastle.crypto.InvalidCipherTextException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnionParsingService @Inject constructor(private val repository: Repository) {

    private val handle: Flow<CryptoContextHandle> = flow {
        repository.local.getKeys().collect {
            val handle = CryptoContext.Factory.createDecryptionContext(it.privateKey, "")
            emit(handle)
        }
    }

    fun parse(ciphertext: String): Flow<Message>
    {
        return flow {
            handle.collect {
                val decodedMessage = Base64.decode(ciphertext, Base64.DEFAULT)
                val message = OnionParser(it).parse(decodedMessage)

                it.dispose()

                if(message != null)
                {
                    emit(message)
                } else {
                    throw InvalidCipherTextException("Ciphertext could not be parsed")
                }
            }
        }
    }
}
