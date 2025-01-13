package ro.aenigma.data

import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.ContactWithConversationPreview
import ro.aenigma.data.database.ContactsDao
import ro.aenigma.data.database.EdgeEntity
import ro.aenigma.data.database.EdgesDao
import ro.aenigma.data.database.GraphVersionEntity
import ro.aenigma.data.database.GraphVersionsDao
import ro.aenigma.data.database.GuardEntity
import ro.aenigma.data.database.GuardsDao
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.database.MessagesDao
import ro.aenigma.data.database.VertexEntity
import ro.aenigma.data.database.VerticesDao
import ro.aenigma.data.network.BaseUrlInterceptor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val contactsDao: ContactsDao,
    private val messagesDao: MessagesDao,
    private val guardsDao: GuardsDao,
    private val verticesDao: VerticesDao,
    private val edgesDao: EdgesDao,
    private val graphVersionsDao: GraphVersionsDao,
    private val preferencesDataStore: PreferencesDataStore,
    private val baseUrlInterceptor: BaseUrlInterceptor
) {
    suspend fun saveNotificationsAllowed(granted: Boolean)
    {
        preferencesDataStore.saveNotificationsAllowed(granted)
    }

    val notificationsAllowed: Flow<Boolean>
    = preferencesDataStore.notificationsAllowed

    fun getContacts() : Flow<List<ContactEntity>>
    {
        return contactsDao.get()
    }

    fun getContactsWithConversationPreviewFlow(): Flow<List<ContactWithConversationPreview>>
    {
        return contactsDao.getWithConversationPreviewFlow()
    }

    suspend fun getContactsWithConversationPreview(): List<ContactWithConversationPreview>
    {
        return contactsDao.getWithConversationPreview()
    }

    suspend fun searchContacts(searchQuery: String = ""): List<ContactEntity>
    {
        return contactsDao.search(searchQuery)
    }

    suspend fun getContact(address: String) : ContactEntity?
    {
        return contactsDao.get(address)
    }

    suspend fun insertOrUpdateContact(contactEntity: ContactEntity)
    {
        contactsDao.insertOrUpdate(contactEntity)
    }

    suspend fun updateContact(contactEntity: ContactEntity)
    {
        contactsDao.update(contactEntity)
    }

    suspend fun removeContacts(contacts: List<ContactEntity>)
    {
        contactsDao.remove(contacts)
    }

    suspend fun getMessage(id: Long): MessageEntity?
    {
        return messagesDao.get(id)
    }

    suspend fun getMessage(uuid: String): MessageEntity?
    {
        return messagesDao.get(uuid)
    }

    fun getConversation(chatId: String) : Flow<List<MessageEntity>>
    {
        return messagesDao.getConversation(chatId)
    }

    suspend fun getConversation(chatId: String, infIndex: Long, searchQuery: String = ""): List<MessageEntity>
    {
        return messagesDao.getConversation(chatId, infIndex, searchQuery)
    }

    suspend fun clearConversation(chatId: String)
    {
        messagesDao.clearConversation(chatId)

        val contact = contactsDao.get(chatId) ?: return
        contact.lastMessageId = null
        contactsDao.update(contact)
    }

    suspend fun removeMessages(messages: List<MessageEntity>, lastMessageId: Long?): Boolean
    {
        if(messages.map { item -> item.chatId }.toSet().size != 1) return false

        messagesDao.remove(messages)
        val contact = contactsDao.get(messages.first().chatId) ?: return true
        contact.lastMessageId = lastMessageId
        contactsDao.update(contact)
        return true
    }

    suspend fun insertMessages(messages: List<MessageEntity>)
    {
        messages.forEach { item ->
            val messageId = messagesDao.insert(item.chatId, item.text, item.incoming, item.sent, item.date, item.dateReceivedOnServer, item.uuid)
            if(messageId > 0)
            {
                val contact = contactsDao.get(item.chatId)
                if (contact != null) {
                    contact.lastMessageId = messageId
                    contactsDao.update(contact)
                }
            }
        }
    }

    fun getOutgoingMessages(): Flow<List<MessageEntity>> {
        return messagesDao.getOutgoingMessages()
    }

    suspend fun updateMessage(message: MessageEntity) {
        return messagesDao.update(message)
    }

    suspend fun markConversationAsUnread(address: String)
    {
        contactsDao.markConversationAsUnread(address)
    }

    suspend fun markConversationAsRead(address: String)
    {
        contactsDao.markConversationAsRead(address)
    }

    suspend fun insertGuard(guard: GuardEntity)
    {
        baseUrlInterceptor.setBaseUrl(guard.hostname)
        return guardsDao.insert(guard)
    }

    suspend fun getGuard(): GuardEntity?
    {
        val guard = guardsDao.getLastGuard()
        if (guard != null) {
            baseUrlInterceptor.setBaseUrl(guard.hostname)
        }
        return guard
    }

    suspend fun getGraphVersion(): GraphVersionEntity?
    {
        return graphVersionsDao.get()
    }

    suspend fun updateGraphVersion(graphVersion: GraphVersionEntity)
    {
        graphVersionsDao.remove()
        graphVersionsDao.insert(graphVersion)
    }

    suspend fun removeVertices()
    {
        return verticesDao.remove()
    }

    suspend fun insertVertices(vertices: List<VertexEntity>)
    {
        return verticesDao.insert(vertices)
    }

    suspend fun getVertices(): List<VertexEntity>
    {
        return verticesDao.get()
    }

    suspend fun removeEdges()
    {
        return edgesDao.remove()
    }

    suspend fun insertEdge(edge: EdgeEntity)
    {
        return edgesDao.insert(edge)
    }

    suspend fun getEdges(): List<EdgeEntity>
    {
        return edgesDao.get()
    }
}
