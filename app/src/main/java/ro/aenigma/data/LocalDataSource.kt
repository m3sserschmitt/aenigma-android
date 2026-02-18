package ro.aenigma.data

import android.content.Context
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import ro.aenigma.data.database.ContactsDao
import ro.aenigma.data.database.EdgesDao
import ro.aenigma.data.database.GuardsDao
import ro.aenigma.data.database.MessagesDao
import ro.aenigma.data.database.VerticesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import ro.aenigma.data.database.extensions.ContactEntityExtensions.toDto
import ro.aenigma.data.database.extensions.ContactWithGroupEntityExtensions.toDto
import ro.aenigma.data.database.extensions.ContactWithLastMessageEntityExtensions.toDto
import ro.aenigma.data.database.extensions.EdgeEntityExtensions.toDto
import ro.aenigma.data.database.extensions.GuardEntityExtensions.toDto
import ro.aenigma.data.database.extensions.MessageEntityExtensions.toDto
import ro.aenigma.data.database.extensions.MessageWithAttachmentsEntityExtensions.toDto
import ro.aenigma.data.database.extensions.MessageWithDetailsEntityExtensions.toArticleDto
import ro.aenigma.data.database.extensions.MessageWithDetailsEntityExtensions.toDto
import ro.aenigma.data.database.extensions.VertexEntityExtensions.toDto
import ro.aenigma.models.ArticleDto
import ro.aenigma.models.AttachmentDto
import ro.aenigma.models.ContactDto
import ro.aenigma.models.ContactWithGroupDto
import ro.aenigma.models.ContactWithLastMessageDto
import ro.aenigma.models.EdgeDto
import ro.aenigma.models.GroupDto
import ro.aenigma.models.GuardDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.MessageWithAttachmentsDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.models.ServerInfoDto
import ro.aenigma.models.VertexDto
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.AttachmentDtoExtensions.toEntity
import ro.aenigma.models.extensions.ContactDtoExtensions.toEntity
import ro.aenigma.models.extensions.ContactDtoExtensions.withLastMessageId
import ro.aenigma.models.extensions.EdgeDtoExtensions.toEntity
import ro.aenigma.models.extensions.GroupDtoExtensions.toEntity
import ro.aenigma.models.extensions.GuardDtoExtensions.toEntity
import ro.aenigma.models.extensions.GuardDtoExtensions.toServerInfoDto
import ro.aenigma.models.extensions.MessageDtoExtensions.markAsDeleted
import ro.aenigma.models.extensions.MessageDtoExtensions.toEntity
import ro.aenigma.models.extensions.VertexDtoExtensions.toEntity
import ro.aenigma.models.factories.MessageDtoFactory
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

    suspend fun getHostname(guard: ServerInfoDto): String? {
        val useTor = useTor.firstOrNull() == true
        return if (useTor) {
            if (guard.onionService.isNullOrBlank()) {
                guard.hostname
            } else {
                guard.onionService
            }
        } else {
            guard.hostname
        }
    }

    suspend fun getHostname(): String? {
        return getHostname(getGuard()?.toServerInfoDto() ?: return null)
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

    suspend fun searchContacts(searchQuery: String = ""): List<ContactDto> {
        return contactsDao.get().search(searchQuery).map { item -> item.toDto() }
    }

    suspend fun getContact(address: String): ContactDto? {
        return contactsDao.get().get(address)?.toDto()
    }

    suspend fun getContactWithGroup(address: String): ContactWithGroupDto? {
        return contactsDao.get().getWithGroup(address)?.toDto()
    }

    fun getContactWithGroupFlow(address: String): Flow<ContactWithGroupDto?> {
        return contactsDao.get().getWithGroupFlow(address).map { item -> item?.toDto() }
    }

    suspend fun insertOrUpdateContact(contactEntity: ContactDto) {
        contactsDao.get().insertOrUpdate(contactEntity.toEntity())
        return updateContactLastMessageId(contactEntity.address)
    }

    suspend fun insertOrIgnoreContact(contactEntity: ContactDto) {
        contactsDao.get().insertOrIgnore(contactEntity.toEntity())
        return updateContactLastMessageId(contactEntity.address)
    }

    suspend fun insertOrUpdateGroup(group: GroupDto) {
        contactsDao.get().insertOrUpdate(group.toEntity())
        return updateContactLastMessageId(group.address)
    }

    suspend fun updateContact(contactEntity: ContactDto) {
        contactsDao.get().update(contactEntity.toEntity())
        return updateContactLastMessageId(contactEntity.address)
    }

    suspend fun removeContacts(contacts: List<ContactDto>) {
        contactsDao.get().remove(contacts.map { item -> item.toEntity() })
        for (contact in contacts) {
            clearConversationSoft(contact.address)
        }
    }

    suspend fun getMessage(id: Long): MessageDto? {
        return messagesDao.get().get(id)?.toDto()
    }

    suspend fun getMessageWithAttachments(id: Long): MessageWithAttachmentsDto? {
        return messagesDao.get().getWithAttachments(id)?.toDto()
    }

    fun getConversationFlow(chatId: String): Flow<List<MessageWithDetailsDto>> {
        return messagesDao.get().getConversationFlow(chatId)
            .map { items -> items.map { item -> item.toDto() } }
    }

    fun getLatestSharedFiles(): Flow<List<ArticleDto>> {
        return messagesDao.get().getLatestSharedFilesFlow()
            .map { items -> items.map { m -> m.toArticleDto(context) } }
    }

    suspend fun getConversationPage(
        chatId: String,
        lastIndex: Long,
        searchQuery: String = ""
    ): List<MessageWithDetailsDto> {
        return messagesDao.get().getConversationPage(chatId, lastIndex, searchQuery)
            .map { item -> item.toDto() }
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
        val contact = contactsDao.get().get(chatId)?.toDto() ?: return
        val lastMessageId = messagesDao.get().getLastMessageId(chatId) ?: messagesDao.get()
            .insertOrIgnore(
                MessageDtoFactory.createOutgoing(
                    chatId = contact.address,
                    text = null,
                    type = MessageType.TEXT,
                    actionFor = null
                ).markAsDeleted().toEntity()
            )
        val updatedContact = contact.withLastMessageId(lastMessageId)
        return contactsDao.get().update(updatedContact.toEntity())
    }

    suspend fun removeMessagesSoft(messages: List<MessageDto>) {
        val chatIds = mutableSetOf<String>()
        for (message in messages) {
            messagesDao.get().removeSoft(message.id)
            removeFiles(message)
            if (chatIds.add(message.chatId)) {
                updateContactLastMessageId(message.chatId)
            }
        }
    }

    private fun removeFiles(message: MessageDto) {
        if (message.incoming) {
            for (file in message.files ?: listOf()) {
                context.deleteUri(file)
            }
        }
    }

    suspend fun removeMessageSoft(refId: String) {
        val message = messagesDao.get().getByRefId(refId)?.toDto() ?: return
        messagesDao.get().removeSoft(refId)
        removeFiles(message)
        updateContactLastMessageId(message.chatId)
    }

    fun getLastDeletedMessage(charId: String): Flow<MessageDto?> {
        return messagesDao.get().getLastDeletedFlow(charId).map { item -> item?.toDto() }
    }

    suspend fun insertOrIgnoreMessage(message: MessageDto): Long? {
        val messageId = messagesDao.get().insertOrIgnore(message.toEntity())
        if (messageId != null && messageId > 0) {
            updateContactLastMessageId(message.chatId)
            if (message.incoming) {
                markConversationAsUnread(message.chatId)
            }
            return messageId
        }
        return null
    }

    suspend fun insertOrUpdateAttachment(attachment: AttachmentDto) {
        return messagesDao.get().insertOrUpdateAttachment(attachment.toEntity())
    }

    suspend fun updateMessage(message: MessageDto) {
        return messagesDao.get().update(message.toEntity())
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

    suspend fun insertOrIgnoreVertices(vertices: List<VertexDto>) {
        return verticesDao.get().insertOrIgnore(vertices.map { item -> item.toEntity() })
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

    suspend fun insertOrIgnoreEdge(edge: EdgeDto) {
        return edgesDao.get().insertOrIgnore(edge.toEntity())
    }

    suspend fun getEdges(): List<EdgeDto> {
        return edgesDao.get().get().map { item -> item.toDto() }
    }
}
