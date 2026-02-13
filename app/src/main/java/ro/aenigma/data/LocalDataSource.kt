package ro.aenigma.data

import android.content.Context
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.ContactsDao
import ro.aenigma.data.database.EdgeEntity
import ro.aenigma.data.database.EdgesDao
import ro.aenigma.data.database.GuardsDao
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.database.MessagesDao
import ro.aenigma.data.database.VertexEntity
import ro.aenigma.data.database.VerticesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import ro.aenigma.data.database.AttachmentEntity
import ro.aenigma.data.database.ContactWithGroup
import ro.aenigma.data.database.GroupEntity
import ro.aenigma.data.database.MessageWithAttachments
import ro.aenigma.data.database.MessageWithDetails
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withLastMessageId
import ro.aenigma.data.database.extensions.ContactWithLastMessageEntityExtensions.toDto
import ro.aenigma.data.database.extensions.GuardEntityExtensions.toDto
import ro.aenigma.data.database.extensions.MessageEntityExtensions.toArticle
import ro.aenigma.data.database.extensions.VertexEntityExtensions.toDto
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.models.ArticleDto
import ro.aenigma.models.ContactDto
import ro.aenigma.models.ContactWithLastMessageDto
import ro.aenigma.models.GuardDto
import ro.aenigma.models.VertexDto
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.ContactDtoExtensions.toEntity
import ro.aenigma.models.extensions.GuardDtoExtensions.toEntity
import ro.aenigma.util.ContextExtensions.deleteUri
import ro.aenigma.util.ContextExtensions.getConversationFilesDir
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    @param:ApplicationContext private val context: Context
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

    suspend fun saveName(name: String): Boolean {
        return preferencesDataStore.saveName(name)
    }

    suspend fun saveTorPreference(useTor: Boolean): Boolean {
        return preferencesDataStore.saveTorPreference(useTor)
    }

    suspend fun saveNotificationsAllowed(granted: Boolean): Boolean {
        return preferencesDataStore.saveNotificationsAllowed(granted)
    }

    suspend fun getHostname(): String? {
        val useTor = useTor.firstOrNull() == true
        val guard = getGuard() ?: return null
        return if(useTor) {
            if(guard.onionService.isNullOrBlank()) {
                guard.hostname
            } else {
                guard.onionService
            }
        } else {
            guard.hostname
        }
    }

    val notificationsAllowed: Flow<Boolean> = preferencesDataStore.notificationsAllowed

    val name: Flow<String> = preferencesDataStore.name

    val useTor: Flow<Boolean> = preferencesDataStore.useTor

    fun getContactWithMessagesFlow(): Flow<List<ContactWithLastMessageDto>> {
        return contactsDao.get().getWithMessagesFlow()
            .map { items -> items.map { item -> item.toDto() } }
    }

    suspend fun getContactWithMessages(): List<ContactWithLastMessageDto> {
        return contactsDao.get().getWithMessages().map { item -> item.toDto() }
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

    fun getContactWithGroupFlow(address: String): Flow<ContactWithGroup?> {
        return contactsDao.get().getWithGroupFlow(address)
    }

    suspend fun insertOrUpdateContact(contactEntity: ContactEntity) {
        contactsDao.get().insertOrUpdate(contactEntity)
        return updateContactLastMessageId(contactEntity.address)
    }

    suspend fun insertOrIgnoreContact(contactEntity: ContactEntity) {
        contactsDao.get().insertOrIgnore(contactEntity)
        return updateContactLastMessageId(contactEntity.address)
    }

    suspend fun insertOrUpdateGroup(group: GroupEntity) {
        contactsDao.get().insertOrUpdate(group)
        return updateContactLastMessageId(group.address)
    }

    suspend fun updateContact(contactEntity: ContactEntity) {
        contactsDao.get().update(contactEntity)
        return updateContactLastMessageId(contactEntity.address)
    }

    suspend fun removeContacts(contacts: List<ContactDto>) {
        contactsDao.get().remove(contacts.map { item -> item.toEntity() })
        for (contact in contacts) {
            clearConversationSoft(contact.address)
        }
    }

    suspend fun getMessage(id: Long): MessageEntity? {
        return messagesDao.get().get(id)
    }

    suspend fun getMessageWithAttachments(id: Long): MessageWithAttachments? {
        return messagesDao.get().getWithAttachments(id)
    }

    fun getConversationFlow(chatId: String): Flow<List<MessageWithDetails>> {
        return messagesDao.get().getConversationFlow(chatId)
    }

    fun getLatestSharedFiles(): Flow<List<ArticleDto>> {
        return messagesDao.get().getLatestSharedFilesFlow()
            .map { items -> items.map { m -> m.toArticle(context) } }
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
        clearConversationFiles(chatId)
        return updateContactLastMessageId(chatId)
    }

    private fun clearConversationFiles(chatId: String): Boolean {
        return try {
            context.getConversationFilesDir(chatId).deleteRecursively()
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun updateContactLastMessageId(chatId: String) {
        val contact = contactsDao.get().get(chatId) ?: return
        val lastMessageId = messagesDao.get().getLastMessageId(chatId) ?: messagesDao.get()
            .insertOrIgnore(
                MessageEntityFactory.createOutgoing(
                    chatId = contact.address,
                    text = null,
                    type = MessageType.TEXT,
                    actionFor = null
                ).copy(deleted = true)
            )
        val updatedContact = contact.withLastMessageId(lastMessageId) ?: return
        return contactsDao.get().update(updatedContact)
    }

    suspend fun removeMessagesSoft(messages: List<MessageEntity>) {
        val chatIds = mutableSetOf<String>()
        for (message in messages) {
            messagesDao.get().removeSoft(message.id)
            removeFiles(message)
            if (chatIds.add(message.chatId)) {
                updateContactLastMessageId(message.chatId)
            }
        }
    }

    private fun removeFiles(message: MessageEntity) {
        if(message.incoming) {
            for (file in message.files ?: listOf()) {
                context.deleteUri(file)
            }
        }
    }

    suspend fun removeMessageSoft(refId: String) {
        val message = messagesDao.get().getByRefId(refId) ?: return
        messagesDao.get().removeSoft(refId)
        removeFiles(message)
        updateContactLastMessageId(message.chatId)
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

    suspend fun insertOrUpdateAttachment(attachment: AttachmentEntity) {
        return messagesDao.get().insertOrUpdateAttachment(attachment)
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

    suspend fun insertGuard(guard: GuardDto) {
        return guardsDao.get().insertWithLimit(guard.toEntity())
    }

    suspend fun getGuard(): GuardDto? {
        return guardsDao.get().getGuard()?.toDto()
    }

    suspend fun getGuards(): List<GuardDto> {
        return guardsDao.get().get().map { item -> item.toDto() }
    }

    fun getGuardsFlow(): Flow<List<GuardDto>> {
        return guardsDao.get().getFlow().map { items -> items.map { item -> item.toDto() } }
    }

    suspend fun searchGuards(query: String): List<GuardDto> {
        return guardsDao.get().search(query).map { item -> item.toDto() }
    }

    suspend fun removeVertices() {
        return verticesDao.get().remove()
    }

    suspend fun insertOrIgnoreVertices(vertices: List<VertexEntity>) {
        return verticesDao.get().insertOrIgnore(vertices)
    }

    suspend fun getVertices(): List<VertexDto> {
        return verticesDao.get().get().map { item -> item.toDto() }
    }

    suspend fun getAllVertices(): List<VertexDto> {
        return verticesDao.get().getAll().map { item -> item.toDto() }
    }

    suspend fun searchVertices(searchQuery: String): List<VertexDto> {
        return verticesDao.get().search(searchQuery).map { item -> item.toDto() }
    }

    fun getVerticesFlow(): Flow<List<VertexDto>> {
        return verticesDao.get().getFlow().map { items -> items.map { item -> item.toDto() } }
    }

    suspend fun removeEdges() {
        return edgesDao.get().remove()
    }

    suspend fun insertOrIgnoreEdge(edge: EdgeEntity) {
        return edgesDao.get().insertOrIgnore(edge)
    }

    suspend fun getEdges(): List<EdgeEntity> {
        return edgesDao.get().get()
    }
}
