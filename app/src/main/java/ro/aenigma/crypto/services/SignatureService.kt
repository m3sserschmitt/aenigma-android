/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

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
