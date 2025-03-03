package ro.aenigma.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.work.WorkInfo
import androidx.work.WorkManager
import ro.aenigma.services.MessageSaver
import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.network.SignalRClient
import ro.aenigma.util.RequestState
import ro.aenigma.util.isFullPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.models.MessageAction
import ro.aenigma.models.MessageActionDto
import ro.aenigma.util.MessageActionType
import ro.aenigma.util.getDescription
import ro.aenigma.workers.MessageSenderWorker
import java.util.SortedSet
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageSaver: MessageSaver,
    private val workManager: WorkManager,
    signatureService: SignatureService,
    signalRClient: SignalRClient,
    repository: Repository,
    application: Application,
) : BaseViewModel(repository, signalRClient, application) {

    private val localAddress = signatureService.address

    private val _conversationSupportListComparator = compareBy<MessageEntity> { item -> item.id }

    // TODO: To be replaced with more efficient approach
    private val _conversationSortedSet: SortedSet<MessageEntity> =
        sortedSetOf(_conversationSupportListComparator)

    private var _filterQuery = MutableStateFlow("")

    private val _allContacts =
        MutableStateFlow<RequestState<List<ContactEntity>>>(RequestState.Idle)

    private val _selectedContact =
        MutableStateFlow<RequestState<ContactEntity>>(RequestState.Idle)

    private val _conversation =
        MutableStateFlow<RequestState<List<MessageEntity>>>(RequestState.Idle)

    private val _replyToMessage = MutableStateFlow<MessageEntity?>(null)

    private val _nextPageAvailable = MutableStateFlow(false)

    private val _messageInputText = MutableStateFlow("")

    val selectedContact: StateFlow<RequestState<ContactEntity>> = _selectedContact

    val conversation: StateFlow<RequestState<List<MessageEntity>>> = _conversation

    val replyToMessage: StateFlow<MessageEntity?> = _replyToMessage

    val messageInputText: StateFlow<String> = _messageInputText

    val nextPageAvailable: StateFlow<Boolean> = _nextPageAvailable

    val allContacts: StateFlow<RequestState<List<ContactEntity>>> = _allContacts

    fun loadContacts(selectedChatId: String) {
        if (_allContacts.value is RequestState.Loading
            || _allContacts.value is RequestState.Success
        ) return

        _allContacts.value = RequestState.Loading
        _selectedContact.value = RequestState.Loading

        collectContacts(selectedChatId)
    }

    private fun collectContacts(selectedChatId: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.local.getContacts().collect { contacts ->
                    _allContacts.value = RequestState.Success(contacts)

                    val selectedContact = contacts.find { item -> item.address == selectedChatId }

                    _selectedContact.value = if (selectedContact != null)
                        RequestState.Success(selectedContact)
                    else
                        RequestState.Error(Exception("Contact not found."))
                }
            } catch (ex: Exception) {
                _allContacts.value = RequestState.Error(ex)
            }
        }
    }

    override fun validateNewContactName(name: String): Boolean {
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
    }

    private fun loadMessageReplyReference(message: MessageEntity) {
        if (message.action.actionType == MessageActionType.REPLY && message.action.refId != null) {
            viewModelScope.launch(ioDispatcher) {
                repository.local.getMessageByRefIdFlow(message.action.refId).collect { replyFor ->
                    if (replyFor != null) {
                        message.responseFor.value = RequestState.Success(replyFor)
                    } else {
                        message.responseFor.value = RequestState.Error(Exception("Not found"))
                    }
                }
            }
        }
    }

    private fun readMessageDeliveryStatus(message: MessageEntity) {
        if(!message.sent) {
            viewModelScope.launch(ioDispatcher) {
                workManager.getWorkInfosForUniqueWorkFlow(
                    MessageSenderWorker.getUniqueWorkRequestName(
                        message.id
                    )
                ).collect { workInfo ->
                    if(workInfo.isNotEmpty()){
                        val lastWorkRequest = workInfo.last()
                        if(lastWorkRequest.state == WorkInfo.State.SUCCEEDED) {
                            message.deliveryStatus.value = true
                        }
                    }
                }
            }
        }
    }

    private fun initialConversationLoad(messages: List<MessageEntity>) {
        messages.forEach { item ->
            loadMessageReplyReference(item)
            readMessageDeliveryStatus(item)
            _conversationSortedSet.add(item)
        }
        _nextPageAvailable.value = messages.isFullPage()
    }

    private fun removeItemFromConversation(refId: String) {
        val itemToBeRemoved = _conversationSortedSet.find { m -> m.refId == refId }
        if (itemToBeRemoved != null) {
            _conversationSortedSet.remove(itemToBeRemoved)
        }
    }

    private fun clearConversation() {
        _conversationSortedSet.clear()
    }

    private fun addNewItemToConversation(message: MessageEntity) {
        loadMessageReplyReference(message)
        if (message.action.refId != null && message.action.actionType == MessageActionType.DELETE) {
            removeItemFromConversation(message.action.refId)
        } else if (message.action.actionType == MessageActionType.DELETE_ALL) {
            clearConversation()
        }
        if(_conversationSortedSet.add(message)) {
            readMessageDeliveryStatus(message)
        }
    }

    private fun setConversation() {
        _conversation.value = RequestState.Success(
            ArrayList(_conversationSortedSet)
        )
    }

    private fun collectConversation(chatId: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.local.getConversation(chatId).collect { messages ->
                    synchronized(_conversationSortedSet) {
                        if (_conversationSortedSet.isEmpty()) {
                            initialConversationLoad(messages)
                        } else if (messages.isNotEmpty() && messages.first().text.contains(
                                _filterQuery.value
                            )
                        ) {
                            addNewItemToConversation(messages.first())
                        }
                        setConversation()
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
                        repository.local.getConversation(chatId, getLastMessageId(), query)
                    synchronized(_conversationSortedSet) {
                        clearConversation()
                        initialConversationLoad(searchResult)
                        setConversation()
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
                val lastIndex = _conversationSortedSet.last().id
                val nextPage =
                    repository.local.getConversation(chatId, lastIndex - 1, _filterQuery.value)
                synchronized(_conversationSortedSet) {
                    if (nextPage.isEmpty()) {
                        _nextPageAvailable.value = false
                    } else {
                        initialConversationLoad(nextPage)
                        setConversation()
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
            postToDatabase(MessageActionDto(MessageActionType.DELETE_ALL, null))
        }
    }

    fun removeMessages(messages: List<MessageEntity>) {
        synchronized(_conversationSortedSet) { _conversationSortedSet.removeAll(messages.toSet()) }
        viewModelScope.launch(ioDispatcher) {
            val textMessages =
                messages.filter { item -> item.action.actionType == MessageActionType.TEXT }
            val textMessagesWithRefs = textMessages.filter { item -> item.refId != null }
            val nonText = messages.filter { item -> item.action.actionType != MessageActionType.TEXT }
            repository.local.removeMessagesSoft(textMessages)
            repository.local.removeMessagesHard(nonText)
            textMessagesWithRefs.forEach { item ->
                postToDatabase(MessageActionDto(MessageActionType.DELETE, item.refId))
            }
        }
    }

    fun setReplyTo(messageEntity: MessageEntity?) {
        _replyToMessage.value = messageEntity
    }

    fun sendMessage() {
        viewModelScope.launch(ioDispatcher) {
            if (postToDatabase()) {
                _messageInputText.value = ""
                _replyToMessage.value = null
            }
        }
    }

    private suspend fun postToDatabase(
        type: MessageActionDto,
        text: String = type.actionType?.getDescription() ?: ""
    ): Boolean {
        try {
            val contact = getSelectedContactEntity() ?: return false
            val action = MessageAction(type.actionType!!, type.refId, localAddress!!)
            val message = MessageEntity(
                chatId = contact.address,
                text = text,
                incoming = false,
                sent = false,
                action = action,
                uuid = null
            )
            return messageSaver.saveOutgoingMessage(message, userName.value)
        } catch (ex: Exception) {
            return false
        }
    }

    private suspend fun postToDatabase(): Boolean {
        if (messageInputText.value.isBlank()) {
            return false
        }
        val type = if (replyToMessage.value != null)
            MessageActionDto(MessageActionType.REPLY, replyToMessage.value?.refId)
        else
            MessageActionDto(MessageActionType.TEXT, null)
        return postToDatabase(type, messageInputText.value)
    }

    private fun getSelectedContactEntity(): ContactEntity? {
        return try {
            (selectedContact.value as RequestState.Success<ContactEntity>).data
        } catch (_: Exception) {
            return null
        }
    }

    private fun getLastMessageId(): Long {
        return try {
            (selectedContact.value as RequestState.Success<ContactEntity>).data.lastMessageId
                ?: 1
        } catch (_: Exception) {
            1
        }
    }

    override fun getContactEntityForSaving(): ContactEntity? {
        return try {
            val contact = (selectedContact.value as RequestState.Success).data
            contact.name = newContactName.value
            contact
        } catch (_: Exception) {
            null
        }
    }

    fun setMessageInputText(text: String) {
        _messageInputText.value = text
    }

    private fun resetSearchQuery() {
        searchConversation("")
    }

    override fun resetContactChanges() {
        resetNewContactName()
    }

    override fun init() {
        resetSearchQuery()
        resetNewContactName()
    }
}
