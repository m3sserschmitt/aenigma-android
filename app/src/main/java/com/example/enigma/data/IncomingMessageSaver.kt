package com.example.enigma.data

import com.example.enigma.crypto.OnionParsingService
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.models.MessageExtended
import com.example.enigma.util.AddressHelper
import com.google.gson.Gson
import javax.inject.Inject

class IncomingMessageSaver @Inject constructor(
    private val repository: Repository,
    private val onionParsingService: OnionParsingService)
{

    suspend fun handleIncomingMessages(messages: List<String>)
    {
        val parsedData = messages.mapNotNull { message -> onionParsingService.parse(message) }
            .mapNotNull { item ->
                try {
                    val gson = Gson()
                    val parsedContent = gson.fromJson(item.text, MessageExtended::class.java)

                    Pair(MessageEntity(item.chatId, parsedContent.text, true, item.date), parsedContent)
                } catch (ex: Exception) {
                    null
                }
            }

        val a = createContacts(parsedData.map { item -> item.second })
        saveMessages(parsedData.map { item -> item.first })
    }

    private suspend fun createContacts(contactsInfo: List<MessageExtended>): List<Long>
    {
        val contacts = contactsInfo.map { item ->
            ContactEntity(
                AddressHelper.getHexAddressFromPublicKey(item.publicKey),
                "",
                item.publicKey,
                item.guardAddress,
                false)
        }

        return repository.local.insertContacts(contacts)
    }

    private suspend fun saveMessages(messages: List<MessageEntity>)
    {
        if(!messages.any()) return

        repository.local.insertMessages(messages)

        val modifiedConversations = messages.map { item -> item.chatId }.toSet()
        modifiedConversations.map { id ->  repository.local.markConversationAsUnread(id)}
    }
}
