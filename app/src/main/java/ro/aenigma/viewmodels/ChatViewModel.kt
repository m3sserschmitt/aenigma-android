package ro.aenigma.viewmodels

import androidx.lifecycle.*
import androidx.work.WorkManager
import ro.aenigma.services.MessageSaver
import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.database.ContactWithGroup
import ro.aenigma.data.database.GroupEntity
import ro.aenigma.data.database.extensions.ContactEntityExtensions.toDto
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withName
import ro.aenigma.data.database.extensions.MessageEntityExtensions.isFullPage
import ro.aenigma.data.database.extensions.MessageEntityExtensions.toDto
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.models.ContactDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.MessageDtoExtensions.toEntity
import ro.aenigma.services.SignalrConnectionController
import ro.aenigma.services.UriBatcher
import ro.aenigma.workers.GroupUploadWorker
import ro.aenigma.workers.MessageSenderWorker
import java.util.SortedSet
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageSaver: MessageSaver,
    private val workManager: WorkManager,
    private val uriBatcher: UriBatcher,
    signatureService: SignatureService,
    signalrConnectionController: SignalrConnectionController,
    repository: Repository,
) : BaseViewModel(repository, signalrConnectionController) {

    private val localAddress = signatureService.address

    private val _conversationSupportListComparator =
        compareByDescending<MessageWithDetailsDto> { item -> item.message.id }

    // TODO: To be replaced with more efficient approach
    private val _conversationSortedSet: SortedSet<MessageWithDetailsDto> =
        sortedSetOf(_conversationSupportListComparator)

    private val _messageSearchQuery = MutableStateFlow("")

    private val _contacts =
        MutableStateFlow<RequestState<List<ContactDto>>>(RequestState.Idle)

    private val _selectedContact =
        MutableStateFlow<RequestState<ContactWithGroup>>(RequestState.Idle)

    private val _isMember = MutableStateFlow(false)

    private val _isAdmin = MutableStateFlow(false)

    private val _conversation =
        MutableStateFlow<RequestState<List<MessageWithDetailsDto>>>(RequestState.Idle)

    private val _replyToMessage =
        MutableStateFlow<RequestState<MessageWithDetailsDto>>(RequestState.Idle)

    private val _nextPageAvailable = MutableStateFlow(false)

    private val _messageInputText = MutableStateFlow("")

    private val _attachments = MutableStateFlow<List<String>>(listOf())

    val selectedContact: StateFlow<RequestState<ContactWithGroup>> = _selectedContact

    val isMember: StateFlow<Boolean> = _isMember

    val isAdmin: StateFlow<Boolean> = _isAdmin

    val conversation: StateFlow<RequestState<List<MessageWithDetailsDto>>> = _conversation

    val replyToMessage: StateFlow<RequestState<MessageWithDetailsDto>> = _replyToMessage

    val messageInputText: StateFlow<String> = _messageInputText

    val attachments: StateFlow<List<String>> = _attachments

    val nextPageAvailable: StateFlow<Boolean> = _nextPageAvailable

    val contacts: StateFlow<RequestState<List<ContactDto>>> = _contacts

    fun searchContacts(searchQuery: String) {
        viewModelScope.launch(ioDispatcher) {
            _contacts.value = RequestState.Loading
            try {
                _contacts.value = RequestState.Success(
                    repository.local.searchContacts(searchQuery).map { c -> c.toDto() })
            } catch (ex: Exception) {
                _contacts.value = RequestState.Error(ex)
            }
        }
    }

    fun collectSelectedContact(selectedChatId: String) {
        viewModelScope.launch(ioDispatcher) {
            _selectedContact.value = RequestState.Loading
            repository.local.getContactWithGroupFlow(selectedChatId).catch { ex ->
                _selectedContact.value = RequestState.Error(ex)
            }.collect { item ->
                try {
                    if (item != null) {
                        _selectedContact.value = RequestState.Success(item)
                        when (item.contact.type) {
                            ContactType.GROUP -> {
                                _isMember.value =
                                    item.group?.groupData?.members?.any { member ->
                                        member.address == localAddress
                                    } == true
                                _isAdmin.value =
                                    item.group?.groupData?.admins?.contains(localAddress) == true
                            }

                            ContactType.CONTACT -> {
                                _isMember.value = true
                                _isAdmin.value = false
                            }
                        }
                    } else {
                        _selectedContact.value = RequestState.Error(Exception("Contact not found."))
                        _isMember.value = false
                        _isAdmin.value = false
                    }
                } catch (ex: Exception) {
                    _selectedContact.value = RequestState.Error(ex)
                }
            }
        }
    }

    fun collectLastDeletedMessage(selectedChatId: String) {
        viewModelScope.launch(ioDispatcher) {
            repository.local.getLastDeletedMessage(selectedChatId).filter { message ->
                message?.type == MessageType.DELETE_ALL
            }.catch { }.collect { clearConversation() }
        }
        viewModelScope.launch(ioDispatcher) {
            repository.local.getLastDeletedMessage(selectedChatId).filter { message ->
                message?.type == MessageType.DELETE
            }.catch { }.collect { message ->
                if (message?.actionFor != null) {
                    removeItemFromConversation(message.actionFor)
                }
            }
        }
    }

    fun loadConversation(chatId: String) {
        collectConversation(chatId)
        collectSearches(chatId)
        collectLastDeletedMessage(chatId)
    }

    private fun loadMessageDeliveryStatus(message: MessageWithDetailsDto) {
        if (!message.message.sent) {
            viewModelScope.launch(ioDispatcher) {
                workManager.getWorkInfosForUniqueWorkFlow(
                    MessageSenderWorker.getUniqueWorkRequestName(
                        message.message.id
                    )
                ).collect { workInfo ->
                    if (workInfo.isNotEmpty()) {
                        message.message.deliveryStatus.value = workInfo.last().state
                    }
                }
            }
        }
    }

    private fun loadActionForSender(message: MessageWithDetailsDto) {
        if (message.actionFor?.senderAddress != null) {
            viewModelScope.launch(ioDispatcher) {
                val actionForSender = repository.local.getContact(message.actionFor.senderAddress)
                if (actionForSender != null) {
                    message.actionForSender.value = actionForSender.toDto()
                } else {
                    message.actionForSender.value = null
                }
            }
        }
    }

    private fun searchFilterMatched(message: MessageWithDetailsDto?): Boolean {
        val filterQuery = _messageSearchQuery.value
        return if (filterQuery.isBlank()) {
            true
        } else {
            message != null && message.message.text != null && message.message.text.contains(
                _messageSearchQuery.value,
                ignoreCase = true
            )
        }
    }

    private fun messageFilesReady(message: MessageWithDetailsDto?): Boolean {
        return message != null && (message.message.type != MessageType.FILES || (message.message.files != null && message.message.files.isNotEmpty()))
    }

    private fun addItemsToConversation(messages: List<MessageWithDetailsDto>) {
        messages.forEach { item ->
            if (searchFilterMatched(item) && messageFilesReady(item)) {
                loadMessageDeliveryStatus(item)
                loadActionForSender(item)
                _conversationSortedSet.add(item)
            }
        }
        _nextPageAvailable.value = messages.isFullPage()
    }

    private fun removeItemFromConversation(refId: String) {
        try {
            val itemToBeRemoved = _conversationSortedSet.find { m -> m.message.refId == refId }
            if (itemToBeRemoved != null) {
                _conversationSortedSet.remove(itemToBeRemoved)
            }
        } catch (_: Exception) {
        }
    }

    private fun clearConversation() {
        _conversationSortedSet.clear()
    }

    private fun addNewItemToConversation(messages: List<MessageWithDetailsDto>) {
        val message = messages.firstOrNull()
        message ?: return
        if (searchFilterMatched(message) && messageFilesReady(message) && _conversationSortedSet.add(
                message
            )
        ) {
            loadMessageDeliveryStatus(message)
            loadActionForSender(message)
        }
    }

    private fun setConversationReadSuccess() {
        _conversation.value = RequestState.Success(
            ArrayList(_conversationSortedSet)
        )
    }

    private fun collectConversation(chatId: String) {
        viewModelScope.launch(ioDispatcher) {
            _conversation.value = RequestState.Loading
            repository.local.getConversationFlow(chatId)
                .map { messages -> messages.map { message -> message.toDto() } }
                .catch { ex -> _conversation.value = RequestState.Error(ex) }
                .collect { messages ->
                    synchronized(_conversationSortedSet) {
                        try {
                            if (_conversationSortedSet.isEmpty()) {
                                addItemsToConversation(messages)
                            } else {
                                addNewItemToConversation(messages)
                            }
                            setConversationReadSuccess()
                        } catch (ex: Exception) {
                            _conversation.value = RequestState.Error(ex)
                        }
                    }
                }
        }
    }

    private fun collectSearches(chatId: String) {
        viewModelScope.launch(ioDispatcher) {
            _messageSearchQuery.collect { query ->
                _conversation.value = RequestState.Loading
                try {
                    val searchResult =
                        repository.local.getConversationPage(chatId, getLastMessageId(), query)
                            .map { item -> item.toDto() }
                    synchronized(_conversationSortedSet) {
                        clearConversation()
                        addItemsToConversation(searchResult)
                        setConversationReadSuccess()
                    }
                } catch (ex: Exception) {
                    _conversation.value = RequestState.Error(ex)
                }
            }
        }
    }

    fun loadNextPage(chatId: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val lastIndex = _conversationSortedSet.last().message.id
                val nextPage =
                    repository.local.getConversationPage(
                        chatId,
                        lastIndex,
                        _messageSearchQuery.value
                    ).map { item -> item.toDto() }
                synchronized(_conversationSortedSet) {
                    if (nextPage.isEmpty()) {
                        _nextPageAvailable.value = false
                    } else {
                        addItemsToConversation(nextPage)
                        setConversationReadSuccess()
                    }
                }
            } catch (ex: Exception) {
                _conversation.value = RequestState.Error(ex)
            }
        }
    }

    fun searchConversation(searchQuery: String) {
        _messageSearchQuery.update { searchQuery }
    }

    fun markConversationAsRead(chatId: String) {
        viewModelScope.launch(ioDispatcher) { repository.local.markConversationAsRead(chatId) }
    }

    fun clearConversation(chatId: String) {
        synchronized(_conversationSortedSet) { _conversationSortedSet.clear() }
        viewModelScope.launch(ioDispatcher) {
            repository.local.clearConversationSoft(chatId)
            postToDatabase(MessageType.DELETE_ALL, null, null)
        }
    }

    fun removeMessages(messages: List<MessageWithDetailsDto>) {
        synchronized(_conversationSortedSet) { _conversationSortedSet.removeAll(messages.toSet()) }
        viewModelScope.launch(ioDispatcher) {
            val textMessagesWithRefs = messages.filter { item -> item.message.refId != null }
            repository.local.removeMessagesSoft(messages.map { item -> item.message.toEntity() })
            textMessagesWithRefs.forEach { item ->
                postToDatabase(MessageType.DELETE, item.message.refId, null)
            }
        }
    }

    fun setReplyTo(message: MessageWithDetailsDto?) {
        if (message == null) {
            _replyToMessage.value = RequestState.Idle
        } else if (message.message.senderAddress != null) {
            viewModelScope.launch(ioDispatcher) {
                _replyToMessage.value = RequestState.Loading
                try {
                    val sender = repository.local.getContact(message.message.senderAddress)
                    _replyToMessage.value = RequestState.Success(
                        MessageWithDetailsDto(
                            message = message.message,
                            sender = sender?.toDto(),
                            actionFor = message.actionFor,
                            actionForSender = message.actionForSender
                        )
                    )
                } catch (ex: Exception) {
                    _replyToMessage.value = RequestState.Error(ex)
                }
            }
        }
    }

    fun sendMessage() {
        viewModelScope.launch(ioDispatcher) { postToDatabase() }
    }

    fun onRetryFailedMessage(message: MessageWithDetailsDto) {
        MessageSenderWorker.createWorkRequest(
            workManager = workManager,
            messageId = message.message.id
        )
    }

    fun editGroupMembers(members: List<String>, action: MessageType) {
        val group = getSelectedGroupEntity()
        if (group != null && group.groupData.name != null) {
            GroupUploadWorker.createOrUpdateGroupWorkRequest(
                workManager = workManager,
                groupName = group.groupData.name,
                members = members,
                existingGroupAddress = group.address,
                actionType = action
            )
        }
    }

    fun leaveGroup() {
        viewModelScope.launch(ioDispatcher) {
            postToDatabase(MessageType.GROUP_MEMBER_LEAVE, null, null)
        }
    }

    private suspend fun postToDatabase(
        type: MessageType,
        actionFor: String?,
        text: String?,
        attachments: List<String> = listOf()
    ): Boolean {
        try {
            val contact = getSelectedContactEntity() ?: return false
            val messages = if (type == MessageType.FILES) {
                uriBatcher.split(attachments).map { batch ->
                    MessageEntityFactory.createOutgoing(
                        chatId = contact.address,
                        text = text,
                        type = type,
                        actionFor = actionFor,
                        attachments = batch
                    )
                }
            } else {
                listOf(
                    MessageEntityFactory.createOutgoing(
                        chatId = contact.address,
                        text = text,
                        type = type,
                        actionFor = actionFor
                    )
                )
            }
            return messageSaver.saveOutgoingMessages(messages)
        } catch (_: Exception) {
            return false
        }
    }

    private suspend fun postToDatabase() {
        if (attachments.value.isNotEmpty()) {
            postToDatabase(MessageType.FILES, null, messageInputText.value, attachments.value)
            _attachments.value = listOf()
            _messageInputText.value = ""
            return
        }
        val hasText = messageInputText.value.isNotBlank()
        if (replyToMessage.value is RequestState.Success && hasText) {
            postToDatabase(
                MessageType.REPLY, getReplyToMessage()?.message?.refId, messageInputText.value
            )
            _replyToMessage.value = RequestState.Idle
            _messageInputText.value = ""
        } else if (hasText) {
            postToDatabase(MessageType.TEXT, null, messageInputText.value)
            _messageInputText.value = ""
        }
    }

    private fun getReplyToMessage(): MessageWithDetailsDto? {
        return try {
            (replyToMessage.value as RequestState.Success<MessageWithDetailsDto>).data
        } catch (_: Exception) {
            return null
        }
    }

    private fun getSelectedContactWithGroup(): ContactWithGroup? {
        return try {
            (selectedContact.value as RequestState.Success<ContactWithGroup>).data
        } catch (_: Exception) {
            return null
        }
    }

    private fun getSelectedContactEntity(): ContactEntity? {
        return getSelectedContactWithGroup()?.contact
    }

    private fun getSelectedGroupEntity(): GroupEntity? {
        return getSelectedContactWithGroup()?.group
    }

    private fun getLastMessageId(): Long {
        return getSelectedContactEntity()?.lastMessageId ?: 1
    }

    fun renameContact(name: String) {
        val contact = getSelectedContactEntity() ?: return
        when (contact.type) {
            ContactType.GROUP -> GroupUploadWorker.createOrUpdateGroupWorkRequest(
                workManager = workManager,
                groupName = name,
                existingGroupAddress = contact.address,
                actionType = MessageType.GROUP_RENAMED,
                members = null
            )

            ContactType.CONTACT -> viewModelScope.launch(ioDispatcher) {
                val updatedContact = contact.withName(name)
                updatedContact?.let { repository.local.updateContact(it) }
            }
        }
    }

    fun setMessageInputText(text: String) {
        _messageInputText.value = text
    }

    fun setAttachments(attachments: List<String>) {
        _attachments.value = attachments
    }

    private fun resetSearchQuery() {
        searchConversation("")
    }

    override fun init() {
        resetSearchQuery()
    }
}
