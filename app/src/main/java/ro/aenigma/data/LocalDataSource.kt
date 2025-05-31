package ro.aenigma.data

import dagger.Lazy
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
import kotlinx.coroutines.flow.Flow
import ro.aenigma.data.database.ContactWithGroup
import ro.aenigma.data.database.ContactWithLastMessage
import ro.aenigma.data.database.GroupEntity
import ro.aenigma.data.database.MessageWithDetails
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withLastMessageId
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) {
    @Inject
    lateinit var contactsDao: Lazy<ContactsDao>

    @Inject
    lateinit var messagesDao: Lazy<MessagesDao>

    @Inject
    lateinit var guardsDao: Lazy<GuardsDao>

    @Inject
    lateinit var verticesDao: Lazy<VerticesDao>

    @Inject
    lateinit var edgesDao: Lazy<EdgesDao>

    @Inject
    lateinit var graphVersionsDao: Lazy<GraphVersionsDao>

    suspend fun saveName(name: String): Boolean {
        return preferencesDataStore.saveName(name)
    }

    suspend fun saveTorPreference(useTor: Boolean): Boolean {
        return preferencesDataStore.saveTorPreference(useTor)
    }

    suspend fun saveNotificationsAllowed(granted: Boolean): Boolean {
        return preferencesDataStore.saveNotificationsAllowed(granted)
    }

    val notificationsAllowed: Flow<Boolean> = preferencesDataStore.notificationsAllowed

    val name: Flow<String> = preferencesDataStore.name

    val useTor: Flow<Boolean> = preferencesDataStore.useTor

    fun getContactsFlow(): Flow<List<ContactEntity>> {
        return contactsDao.get().getFlow()
    }

    fun getContactWithMessagesFlow(): Flow<List<ContactWithLastMessage>> {
        return contactsDao.get().getWithMessagesFlow()
    }

    suspend fun getContactWithMessages(): List<ContactWithLastMessage> {
        return contactsDao.get().getWithMessages()
    }

    suspend fun searchContacts(searchQuery: String = ""): List<ContactEntity> {
        return contactsDao.get().search(searchQuery)
    }

    suspend fun getContact(address: String): ContactEntity? {
        return contactsDao.get().get(address)
    }

    suspend fun getContactWithGroup(address: String): ContactWithGroup? {
        return contactsDao.get().getWithGroup(address)
    }

    suspend fun getContactsWithGroup(): List<ContactWithGroup> {
        return contactsDao.get().getWithGroup()
    }

    fun getContactWithGroupFlow(address: String): Flow<ContactWithGroup?> {
        return contactsDao.get().getWithGroupFlow(address)
    }

    suspend fun insertOrUpdateContact(contactEntity: ContactEntity) {
        return contactsDao.get().insertOrUpdate(contactEntity)
    }

    suspend fun insertOrIgnoreContact(contactEntity: ContactEntity) {
        return contactsDao.get().insertOrIgnore(contactEntity)
    }

    suspend fun insertOrUpdateGroup(group: GroupEntity) {
        return contactsDao.get().insertOrUpdate(group)
    }

    suspend fun updateContact(contactEntity: ContactEntity) {
        contactsDao.get().update(contactEntity)
    }

    suspend fun removeContacts(contacts: List<ContactEntity>) {
        contactsDao.get().remove(contacts)
    }

    suspend fun getMessage(id: Long): MessageEntity? {
        return messagesDao.get().get(id)
    }

    fun getConversationFlow(chatId: String): Flow<List<MessageWithDetails>> {
        return messagesDao.get().getConversationFlow(chatId)
    }

    suspend fun getMessageByUuid(uuid: String): MessageEntity? {
        return messagesDao.get().getByServerUuid(uuid)
    }

    suspend fun getMessageByRefId(refId: String): MessageEntity? {
        return messagesDao.get().getByRefId(refId)
    }

    suspend fun getConversationPage(
        chatId: String,
        lastIndex: Long,
        searchQuery: String = ""
    ): List<MessageWithDetails> {
        return messagesDao.get().getConversationPage(chatId, lastIndex, searchQuery)
    }

    suspend fun clearConversationSoft(chatId: String) {
        messagesDao.get().clearConversationSoft(chatId)
        updateContactLastMessageId(chatId)
    }

    private suspend fun updateContactLastMessageId(chatId: String) {
        val contact = contactsDao.get().get(chatId) ?: return
        val updatedContact =
            contact.withLastMessageId(messagesDao.get().getLastMessageId(chatId)) ?: return
        return contactsDao.get().update(updatedContact)
    }

    suspend fun removeMessagesSoft(messages: List<MessageEntity>) {
        if (messages.map { item -> item.chatId }.toSet().size != 1) return
        messagesDao.get().removeSoft(messages.map { item -> item.id })
        val chatId = messages.first().chatId
        updateContactLastMessageId(chatId)
    }

    suspend fun removeMessageSoft(refId: String) {
        val message = messagesDao.get().getByRefId(refId) ?: return
        messagesDao.get().removeSoft(refId)
        updateContactLastMessageId(message.chatId)
    }

    suspend fun removeMessagesHard() {
        return messagesDao.get().removeHard()
    }

    fun getLastDeletedMessage(charId: String): Flow<MessageEntity?> {
        return messagesDao.get().getLastDeletedFlow(charId)
    }

    suspend fun insertOrIgnoreMessage(message: MessageEntity): Long? {
        val messageId = messagesDao.get().insertOrIgnore(message)
        if (messageId != null && messageId > 0) {
            updateContactLastMessageId(message.chatId)
            if (message.incoming) {
                markConversationAsUnread(message.chatId)
            }
            return messageId
        }
        return null
    }

    suspend fun updateMessage(message: MessageEntity) {
        return messagesDao.get().update(message)
    }

    suspend fun markConversationAsUnread(address: String) {
        return contactsDao.get().markConversationAsUnread(address)
    }

    suspend fun markConversationAsRead(address: String) {
        return contactsDao.get().markConversationAsRead(address)
    }

    suspend fun insertGuard(guard: GuardEntity) {
        return guardsDao.get().insert(guard)
    }

    suspend fun getGuard(): GuardEntity? {
        return guardsDao.get().getLastGuard()
    }

    suspend fun getGraphVersion(): GraphVersionEntity? {
        return graphVersionsDao.get().get()
    }

    suspend fun updateGraphVersion(graphVersion: GraphVersionEntity) {
        graphVersionsDao.get().remove()
        graphVersionsDao.get().insert(graphVersion)
    }

    suspend fun removeVertices() {
        return verticesDao.get().remove()
    }

    suspend fun insertVertices(vertices: List<VertexEntity>) {
        return verticesDao.get().insert(vertices)
    }

    suspend fun getVertices(): List<VertexEntity> {
        return verticesDao.get().get()
    }

    suspend fun removeEdges() {
        return edgesDao.get().remove()
    }

    suspend fun insertEdge(edge: EdgeEntity) {
        return edgesDao.get().insert(edge)
    }

    suspend fun getEdges(): List<EdgeEntity> {
        return edgesDao.get().get()
    }
}
