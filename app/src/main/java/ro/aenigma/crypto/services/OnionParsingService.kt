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
import ro.aenigma.models.ParsedMessageDto
import ro.aenigma.models.PendingMessageDto
import ro.aenigma.models.hubInvocation.RoutingRequest
import ro.aenigma.util.Constants
import ro.aenigma.util.HexConverter
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.KeysManager
import ro.aenigma.util.ZonedDateTimeExtensions.normalize
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnionParsingService @Inject constructor(keysManager: KeysManager) {

    private val mutex = Mutex()

    private var ready = false

    init {
        val key = keysManager.readPrivateKey()
        ready = key != null && CryptoProvider.initDecryptionEx(key)
    }

    suspend fun parse(routingRequest: RoutingRequest): List<ParsedMessageDto> {
        return routingRequest.payloads?.mapNotNull { item ->
            parse(
                PendingMessageDto(
                    id = null,
                    uuid = routingRequest.uuid,
                    destination = null,
                    content = item,
                    dateReceived = ZonedDateTime.now().toString(),
                    sent = false
                )
            )
        } ?: listOf()
    }

    suspend fun parse(pendingMessageDto: PendingMessageDto): ParsedMessageDto? {
        if (!ready || pendingMessageDto.content == null) {
            return null
        }
        mutex.withLock {
            return try {
                val decryptedData =
                    CryptoProvider.unsealOnionEx(pendingMessageDto.content) ?: return null
                if (decryptedData.size < Constants.ADDRESS_SIZE_BYTES + 1) {
                    return null
                }
                val chatId =
                    HexConverter.toHex(decryptedData.sliceArray(0 until Constants.ADDRESS_SIZE_BYTES))
                val content =
                    String(decryptedData.sliceArray(Constants.ADDRESS_SIZE_BYTES until decryptedData.size))
                val dateReceivedOnServer = pendingMessageDto.dateReceived?.normalize()
                ParsedMessageDto(chatId, content, dateReceivedOnServer, pendingMessageDto.uuid)
            } catch (_: Exception) {
                null
            }
        }
    }
}
