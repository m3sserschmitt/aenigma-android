package com.example.enigma.crypto

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SignatureService @Inject constructor(@ApplicationContext context: Context) {

    private val publicKey: String? = KeysManager.readPublicKey(context)

    private val privateKey: String? = KeysManager.readPrivateKey(context)

    fun sign(data: ByteArray): Pair<String, String>?
    {
        val signature = if(privateKey != null && publicKey != null)
            CryptoProvider.sign(privateKey, "", data) else null
        val encodedSignature = if(signature != null)
            Base64.encodeToString(signature, Base64.DEFAULT) else null

        return if (encodedSignature != null && publicKey != null)
            Pair(publicKey, encodedSignature) else null
    }
}
