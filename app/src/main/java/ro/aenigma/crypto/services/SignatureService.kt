package ro.aenigma.crypto.services

import android.content.Context
import ro.aenigma.crypto.extensions.PublicKeyExtensions.getAddressFromPublicKey
import dagger.hilt.android.qualifiers.ApplicationContext
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.KeysManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignatureService @Inject constructor(@ApplicationContext context: Context) {

    private var _publicKey: String? = null

    private var _address: String? = null

    val publicKey: String?
        get() {
            return _publicKey
        }

    val address: String?
        get() {
            return _address
        }

    private var ready = false

    init {
        if (KeysManager.generateKeyIfNotExistent(context)) {
            _publicKey = KeysManager.readPublicKey(context)
            _address = _publicKey.getAddressFromPublicKey()
            val privateKey = KeysManager.readPrivateKey(context)
            if (privateKey != null) {
                ready = CryptoProvider.initSignatureEx(privateKey)
            }
        }
    }

    fun sign(data: ByteArray): Pair<String, String>? {
        if (_publicKey == null || !ready) {
            return null
        }
        synchronized(_publicKey!!)
        {
            return try {
                val signature = CryptoProvider.signEx(data)
                if (signature != null) Pair(_publicKey!!, signature) else null
            } catch (_: Exception) {
                null
            }
        }
    }
}
