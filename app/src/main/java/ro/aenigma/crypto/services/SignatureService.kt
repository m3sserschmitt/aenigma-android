package ro.aenigma.crypto.services

import android.content.Context
import ro.aenigma.crypto.PublicKeyExtensions.getAddressFromPublicKey
import dagger.hilt.android.qualifiers.ApplicationContext
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.KeysManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignatureService @Inject constructor(@ApplicationContext context: Context) {

    val publicKey: String? = KeysManager.readPublicKey(context)

    val address: String? = publicKey.getAddressFromPublicKey()

    private var ready = false

    init {
        val privateKey = KeysManager.readPrivateKey(context)
        if (privateKey != null) {
            ready = CryptoProvider.initSignatureEx(privateKey)
        }
    }

    fun sign(data: ByteArray): Pair<String, String>? {
        if (publicKey == null || !ready) {
            return null
        }
        synchronized(publicKey)
        {
            return try {
                val signature = CryptoProvider.signEx(data)
                if (signature != null) Pair(publicKey, signature) else null
            } catch (_: Exception) {
                null
            }
        }
    }
}
