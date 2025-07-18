package ro.aenigma.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.work.WorkInfo
import androidx.work.WorkManager
import ro.aenigma.services.MessageSaver
import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.database.ContactWithGroup
import ro.aenigma.data.database.GroupEntity
import ro.aenigma.data.database.MessageWithDetails
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withName
import ro.aenigma.data.database.extensions.MessageEntityExtensions.isFullPage
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.services.SignalrConnectionController
import ro.aenigma.workers.GroupUploadWorker
import ro.aenigma.workers.MessageSenderWorker
import java.util.SortedSet
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageSaver: MessageSaver,
    private val workManager: WorkManager,
    signatureService: SignatureService,
    signalrConnectionController: SignalrConnectionController,
    repository: Repository,
    application: Application,
) : BaseViewModel(repository, signalrConnectionController, application) {

    private val localAddress = signatureService.address

    private val _conversationSupportListComparator =
        compareByDescending<MessageWithDetails> { item -> item.message.id }

    // TODO: To be replaced with more efficient approach
    private val _conversationSortedSet: SortedSet<MessageWithDetails> =
        sortedSetOf(_conversationSupportListComparator)

    private var _filterQuery = MutableStateFlow("")

    private val _allContacts =
        MutableStateFlow<RequestState<List<ContactEntity>>>(RequestState.Idle)

    private val _selectedContact =
        MutableStateFlow<RequestState<ContactWithGroup>>(RequestState.Idle)

    private val _isMember = MutableStateFlow(false)

    private val _isAdmin = MutableStateFlow(false)

    private val _conversation =
        MutableStateFlow<RequestState<List<MessageWithDetails>>>(RequestState.Idle)

    private val _replyToMessage = MutableStateFlow<MessageWithDetails?>(null)

    private val _nextPageAvailable = MutableStateFlow(false)

    private val _messageInputText = MutableStateFlow("")

    private val _attachments = MutableStateFlow<List<String>>(listOf())

    val selectedContact: StateFlow<RequestState<ContactWithGroup>> = _selectedContact

    val isMember: StateFlow<Boolean> = _isMember

    val isAdmin: StateFlow<Boolean> = _isAdmin

    val conversation: StateFlow<RequestState<List<MessageWithDetails>>> = _conversation

    val replyToMessage: StateFlow<MessageWithDetails?> = _replyToMessage

    val messageInputText: StateFlow<String> = _messageInputText

    val attachments: StateFlow<List<String>> = _attachments

    val nextPageAvailable: StateFlow<Boolean> = _nextPageAvailable

    val allContacts: StateFlow<RequestState<List<ContactEntity>>> = _allContacts

    fun loadContacts(selectedChatId: String) {
        if (_allContacts.value is RequestState.Loading
            || _allContacts.value is RequestState.Success
        ) return

        _allContacts.value = RequestState.Loading
        _selectedContact.value = RequestState.Loading

        collectContacts()
        collectSelectedContact(selectedChatId)
    }

    private fun collectContacts() {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.local.getContactsFlow().collect { contacts ->
                    _allContacts.value = RequestState.Success(contacts)
                }
            } catch (ex: Exception) {
                _allContacts.value = RequestState.Error(ex)
            }
        }
    }

    private fun collectSelectedContact(selectedChatId: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.local.getContactWithGroupFlow(selectedChatId).collect { item ->
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
                }
            } catch (ex: Exception) {
                _selectedContact.value = RequestState.Error(ex)
            }
        }
    }

    fun collectLastDeletedMessage(selectedChatId: String) {
        viewModelScope.launch(ioDispatcher) {
            repository.local.getLastDeletedMessage(selectedChatId).filter { message ->
                message?.type == MessageType.DELETE_ALL
            }.collect { clearConversation() }
        }
        viewModelScope.launch(ioDispatcher) {
            repository.local.getLastDeletedMessage(selectedChatId).filter { message ->
                message?.type == MessageType.DELETE
            }.collect { message ->
                if (message?.actionFor != null) {
                    removeItemFromConversation(message.actionFor)
                }
            }
        }
    }

    fun validateNewContactName(name: String): Boolean {
        return name.isNotBlank() && try {
            (_allContacts.value as RequestState.Success).data.all { item -> item.name != name }
        } catch (_: Exception) {
            false
        }
    }

    fun loadConversation(chatId: String) {
        if (_conversation.value is RequestState.Success
            || _conversation.value is RequestState.Loading
        ) return

        _conversation.value = RequestState.Loading

        collectConversation(chatId)
        collectSearches(chatId)
        collectLastDeletedMessage(chatId)
    }

    private fun readMessageDeliveryStatus(message: MessageWithDetails) {
        if (!message.message.sent) {
            viewModelScope.launch(ioDispatcher) {
                workManager.getWorkInfosForUniqueWorkFlow(
                    MessageSenderWorker.getUniqueWorkRequestName(
                        message.message.id
                    )
                ).collect { workInfo ->
                    if (workInfo.isNotEmpty()) {
                        val lastWorkRequest = workInfo.last()
                        if (lastWorkRequest.state == WorkInfo.State.SUCCEEDED) {
                            message.message.deliveryStatus.value = true
                        }
                    }
                }
            }
        }
    }

    private fun searchFilterMatched(message: MessageWithDetails?): Boolean {
        val filterQuery = _filterQuery.value
        return if (filterQuery.isBlank()) {
            true
        } else {
            message != null && message.message.text != null && message.message.text.contains(
                _filterQuery.value,
                ignoreCase = true
            )
        }
    }

    private fun messageFilesReady(message: MessageWithDetails?): Boolean {
        return message != null && (message.message.type != MessageType.FILES || (message.message.files != null && message.message.files.isNotEmpty()))
    }

    private fun addItemsToConversation(messages: List<MessageWithDetails>) {
        messages.forEach { item ->
            if (searchFilterMatched(item) && messageFilesReady(item)) {
                readMessageDeliveryStatus(item)
                _conversationSortedSet.add(item)
            }
        }
        _nextPageAvailable.value = messages.isFullPage()
    }

    private fun removeItemFromConversation(refId: String) {
        val itemToBeRemoved = _conversationSortedSet.find { m -> m.message.refId == refId }
        if (itemToBeRemoved != null) {
            _conversationSortedSet.remove(itemToBeRemoved)
        }
    }

    private fun clearConversation() {
        _conversationSortedSet.clear()
    }

    private fun addNewItemToConversation(messages: List<MessageWithDetails>) {
        val message = messages.firstOrNull()
        message ?: return
        if (searchFilterMatched(message) && messageFilesReady(message) && _conversationSortedSet.add(
                message
            )
        ) {
            readMessageDeliveryStatus(message)
        }
    }

    private fun setConversationReadSuccess() {
        _conversation.value = RequestState.Success(
            ArrayList(_conversationSortedSet)
        )
    }

    private fun collectConversation(chatId: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.local.getConversationFlow(chatId).collect { messages ->
                    synchronized(_conversationSortedSet) {
                        if (_conversationSortedSet.isEmpty()) {
                            addItemsToConversation(messages)
                        } else {
                            addNewItemToConversation(messages)
                        }
                        setConversationReadSuccess()
                    }
                }
            } catch (ex: Exception) {
                _conversation.value = RequestState.Error(ex)
            }
        }
    }

    private fun collectSearches(chatId: String) {
        viewModelScope.launch(ioDispatcher) {
            _filterQuery.collect { query ->
                _conversation.value = RequestState.Loading
                try {
                    val searchResult =
                        repository.local.getConversationPage(chatId, getLastMessageId(), query)
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
                    repository.local.getConversationPage(chatId, lastIndex, _filterQuery.value)
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
        _filterQuery.update { searchQuery }
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

    fun removeMessages(messages: List<MessageWithDetails>) {
        synchronized(_conversationSortedSet) { _conversationSortedSet.removeAll(messages.toSet()) }
        viewModelScope.launch(ioDispatcher) {
            val textMessagesWithRefs = messages.filter { item -> item.message.refId != null }
            repository.local.removeMessagesSoft(messages.map { item -> item.message })
            textMessagesWithRefs.forEach { item ->
                postToDatabase(MessageType.DELETE, item.message.refId, null)
            }
        }
    }

    fun setReplyTo(messageEntity: MessageWithDetails?) {
        _replyToMessage.value = messageEntity
    }

    fun sendMessage() {
        viewModelScope.launch(ioDispatcher) { postToDatabase() }
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
            val message = MessageEntityFactory.createOutgoing(
                chatId = contact.address,
                text = text,
                type = type,
                actionFor = actionFor,
                attachments = attachments
            )
            return messageSaver.saveOutgoingMessage(message)
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
        if (replyToMessage.value != null && hasText) {
            postToDatabase(
                MessageType.REPLY, replyToMessage.value?.message?.refId, messageInputText.value
            )
            _replyToMessage.value = null
            _messageInputText.value = ""
        } else if (hasText) {
            postToDatabase(MessageType.TEXT, null, messageInputText.value)
            _messageInputText.value = ""
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
