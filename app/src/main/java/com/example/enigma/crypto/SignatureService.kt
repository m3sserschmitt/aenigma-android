package com.example.enigma.crypto

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SignatureService @Inject constructor(@ApplicationContext context: Context) {

    private val publicKey: String? = KeysManager.readPublicKey(context)

    private val privateKey: String? = KeysManager.readPrivateKey(context)

    fun sign(data: ByteArray): Pair<String, String>? {
        if(publicKey == null || privateKey == null)
        {
            return null
        }
        synchronized(publicKey)
        {
            val signature = CryptoProvider.sign(privateKey, "", data)
            val encodedSignature = if (signature != null)
                Base64.encodeToString(signature, Base64.DEFAULT) else null

            return if (encodedSignature != null)
                Pair(publicKey, encodedSignature) else null
        }
    }
}
