package com.example.enigma.crypto

import android.util.Base64
import com.example.enigma.data.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignatureService @Inject constructor(private val repository: Repository) {

    fun sign(token: String): Flow<Pair<String, String>>
    {
        return flow {
            repository.local.getKeys().collect {

                val decodedToken = Base64.decode(token, Base64.DEFAULT)
                val signature = CryptoProvider.sign(it.privateKey, "", decodedToken)

                emit(Pair(it.publicKey, Base64.encodeToString(signature, Base64.DEFAULT)))
            }
        }
    }
}
