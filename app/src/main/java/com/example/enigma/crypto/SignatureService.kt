package com.example.enigma.crypto

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SignatureService @Inject constructor(@ApplicationContext context: Context) {

    private val publicKey: String?

    private val privateKey: String?

    init {
        publicKey = KeysManager.readPublicKey(context)
        privateKey = KeysManager.readPrivateKey(context)
    }
    fun sign(token: String): Pair<String, String>?
    {

        val decodedToken = Base64.decode(token, Base64.DEFAULT)
        val signature = if(privateKey != null && publicKey != null)
            CryptoProvider.sign(privateKey, "", decodedToken) else null
        val encodedSignature = if(signature != null)
            Base64.encodeToString(signature, Base64.DEFAULT) else null

        return if (encodedSignature != null && publicKey != null)
            Pair(publicKey, encodedSignature) else null
    }
}
