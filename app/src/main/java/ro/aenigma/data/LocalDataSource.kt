package ro.aenigma.data

import ro.aenigma.data.database.ContactEntity
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
import ro.aenigma.data.database.ContactWithGroup
import ro.aenigma.data.database.ContactWithLastMessage
import ro.aenigma.data.database.GroupEntity
import ro.aenigma.data.database.MessageWithDetails
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withLastMessageId
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
    suspend fun saveName(name: String)
    {
        return preferencesDataStore.saveName(name)
    }

    suspend fun saveNotificationsAllowed(granted: Boolean) {
        preferencesDataStore.saveNotificationsAllowed(granted)
    }

    val notificationsAllowed: Flow<Boolean> = preferencesDataStore.notificationsAllowed

    val name: Flow<String> = preferencesDataStore.name

    fun getContactsFlow(): Flow<List<ContactEntity>> {
        return contactsDao.getFlow()
    }

    fun getContactWithMessagesFlow(): Flow<List<ContactWithLastMessage>> {
        return contactsDao.getWithMessagesFlow()
    }

    suspend fun getContactWithMessages(): List<ContactWithLastMessage> {
        return contactsDao.getWithMessages()
    }

    suspend fun searchContacts(searchQuery: String = ""): List<ContactEntity> {
        return contactsDao.search(searchQuery)
    }

    suspend fun getContact(address: String): ContactEntity? {
        return contactsDao.get(address)
    }

    suspend fun getContactWithGroup(address: String): ContactWithGroup? {
        return contactsDao.getWithGroup(address)
    }

    fun getContactWithGroupFlow(address: String): Flow<ContactWithGroup?> {
        return contactsDao.getWithGroupFlow(address)
    }

    suspend fun insertOrUpdateContact(contactEntity: ContactEntity) {
        return contactsDao.insertOrUpdate(contactEntity)
    }

    suspend fun insertOrIgnoreContact(contactEntity: ContactEntity) {
        return contactsDao.insertOrIgnore(contactEntity)
    }

    suspend fun insertOrUpdateGroup(group: GroupEntity) {
        return contactsDao.insertOrUpdate(group)
    }

    suspend fun updateContact(contactEntity: ContactEntity) {
        contactsDao.update(contactEntity)
    }

    suspend fun removeContacts(contacts: List<ContactEntity>) {
        contactsDao.remove(contacts)
    }

    suspend fun getMessage(id: Long): MessageEntity? {
        return messagesDao.get(id)
    }

    fun getConversationFlow(chatId: String): Flow<List<MessageWithDetails>> {
        return messagesDao.getConversationFlow(chatId)
    }

    suspend fun getMessageByUuid(uuid: String): MessageEntity? {
        return messagesDao.getByServerUuid(uuid)
    }

    suspend fun getConversation(
        chatId: String,
        infIndex: Long,
        searchQuery: String = ""
    ): List<MessageWithDetails> {
        return messagesDao.getConversation(chatId, infIndex, searchQuery)
    }

    suspend fun clearConversationSoft(chatId: String) {
        messagesDao.clearConversationSoft(chatId)
        updateContactLastMessageId(chatId)
    }

    private suspend fun updateContactLastMessageId(chatId: String) {
        val contact = contactsDao.get(chatId) ?: return
        val updatedContact = contact.withLastMessageId(messagesDao.getLastMessageId(chatId)) ?: return
        return contactsDao.update(updatedContact)
    }

    suspend fun removeMessagesSoft(messages: List<MessageEntity>) {
        if (messages.map { item -> item.chatId }.toSet().size != 1) return
        messagesDao.removeSoft(messages.map { item -> item.id })
        val chatId = messages.first().chatId
        updateContactLastMessageId(chatId)
    }

    suspend fun removeMessageSoft(refId: String) {
        val message = messagesDao.getByRefId(refId) ?: return
        messagesDao.removeSoft(refId)
        updateContactLastMessageId(message.chatId)
    }

    suspend fun removeMessagesHard() {
        return messagesDao.removeHard()
    }

    suspend fun removeMessagesHard(messages: List<MessageEntity>) {
        return messagesDao.removeHard(messages)
    }

    suspend fun insertMessage(message: MessageEntity): Long {
        val messageId = messagesDao.insertOrIgnore(
            chatId = message.chatId,
            senderAddress = message.senderAddress,
            serverUUID = message.serverUUID,
            text = message.text,
            type = message.type,
            actionFor = message.actionFor,
            refId = message.refId,
            incoming = message.incoming,
            date = message.date,
            dateReceivedOnServer = message.dateReceivedOnServer,
        )
        if (messageId != null && messageId > 0) {
            updateContactLastMessageId(message.chatId)
            if (message.incoming) {
                markConversationAsUnread(message.chatId)
            }
            return messageId
        }
        return -1
    }

    suspend fun updateMessage(message: MessageEntity) {
        return messagesDao.update(message)
    }

    suspend fun markConversationAsUnread(address: String) {
        return contactsDao.markConversationAsUnread(address)
    }

    suspend fun markConversationAsRead(address: String) {
        return contactsDao.markConversationAsRead(address)
    }

    suspend fun insertGuard(guard: GuardEntity) {
        baseUrlInterceptor.setBaseUrl(guard.hostname)
        return guardsDao.insert(guard)
    }

    suspend fun getGuard(): GuardEntity? {
        val guard = guardsDao.getLastGuard()
        if (guard != null) {
            baseUrlInterceptor.setBaseUrl(guard.hostname)
        }
        return guard
    }

    suspend fun getGraphVersion(): GraphVersionEntity? {
        return graphVersionsDao.get()
    }

    suspend fun updateGraphVersion(graphVersion: GraphVersionEntity) {
        graphVersionsDao.remove()
        graphVersionsDao.insert(graphVersion)
    }

    suspend fun removeVertices() {
        return verticesDao.remove()
    }

    suspend fun insertVertices(vertices: List<VertexEntity>) {
        return verticesDao.insert(vertices)
    }

    suspend fun getVertices(): List<VertexEntity> {
        return verticesDao.get()
    }

    suspend fun removeEdges() {
        return edgesDao.remove()
    }

    suspend fun insertEdge(edge: EdgeEntity) {
        return edgesDao.insert(edge)
    }

    suspend fun getEdges(): List<EdgeEntity> {
        return edgesDao.get()
    }
}