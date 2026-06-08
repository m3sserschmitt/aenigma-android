package ro.aenigma.crypto.services

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ro.aenigma.crypto.extensions.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.KeysManager
import ro.aenigma.crypto.extensions.SignatureExtensions.jsonSign
import ro.aenigma.models.SignatureDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignatureService @Inject constructor(keysManager: KeysManager) {

    private val _mutex = Mutex()

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
        ready = _publicKey != null && _address != null && privateKey != null
                && CryptoProvider.initSignatureEx(privateKey)
    }

    suspend fun sign(data: ByteArray): SignatureDto = _mutex.withLock {
        if (!ready) {
            return SignatureDto(_publicKey, null)
        }

        return try {
            SignatureDto(_publicKey, CryptoProvider.signEx(data))
        } catch (_: Exception) {
            SignatureDto(_publicKey, null)
        }

    }

    suspend inline fun <reified T> jsonSign(data: T): SignatureDto? {
        return data.jsonSign(this)
    }
}
