package ro.aenigma.crypto.services

import ro.aenigma.crypto.extensions.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.KeysManager
import ro.aenigma.crypto.extensions.SignatureExtensions.jsonSign
import ro.aenigma.models.SignatureDto
import ro.aenigma.models.SignedData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignatureService @Inject constructor(keysManager: KeysManager) {

    private val lock = Any()

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
        _publicKey = keysManager.readPublicKey()
        _address = _publicKey.getAddressFromPublicKey()
        val privateKey = keysManager.readPrivateKey()
        ready =
            _publicKey != null && _address != null && privateKey != null && CryptoProvider.initSignatureEx(
                privateKey
            )
    }

    fun sign(data: ByteArray): SignatureDto {
        if (!ready) {
            return SignatureDto(_publicKey, null)
        }
        synchronized(lock)
        {
            return try {
                SignatureDto(_publicKey, CryptoProvider.signEx(data))
            } catch (_: Exception) {
                SignatureDto(_publicKey, null)
            }
        }
    }

    inline fun <reified T> jsonSign(data: T): SignedData? {
        return data.jsonSign(this)
    }
}
