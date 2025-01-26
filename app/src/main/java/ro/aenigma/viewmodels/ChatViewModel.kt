package ro.aenigma.viewmodels

import android.app.Application
import androidx.lifecycle.*
import ro.aenigma.data.MessageSaver
import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.network.SignalRClient
import ro.aenigma.util.DatabaseRequestState
import ro.aenigma.util.isFullPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ro.aenigma.models.MessageAction
import ro.aenigma.util.MessageActionType
import java.util.SortedSet
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageSaver: MessageSaver,
    signalRClient: SignalRClient,
    repository: Repository,
    application: Application,
) : BaseViewModel(repository, signalRClient, application) {

    private val _conversationSupportListComparator = compareBy<MessageEntity> { item -> item.id }

    // TODO: To be replaced with more efficient approach
    private val _conversationSortedSet: SortedSet<MessageEntity> =
        sortedSetOf(_conversationSupportListComparator)

    private var _filterQuery = MutableStateFlow("")

    private val _contactNames =
        MutableStateFlow<DatabaseRequestState<List<String>>>(DatabaseRequestState.Idle)

    private val _selectedContact =
        MutableStateFlow<DatabaseRequestState<ContactEntity>>(DatabaseRequestState.Idle)

    private val _conversation =
        MutableStateFlow<DatabaseRequestState<List<MessageEntity>>>(DatabaseRequestState.Idle)

    private val _replyToMessage = MutableStateFlow<MessageEntity?>(null)

    private val _notSentMessages = MutableStateFlow<List<MessageEntity>>(listOf())

    private val _nextPageAvailable = MutableStateFlow(false)

    private val _messageInputText = MutableStateFlow("")

    val selectedContact: StateFlow<DatabaseRequestState<ContactEntity>> = _selectedContact

    val conversation: StateFlow<DatabaseRequestState<List<MessageEntity>>> = _conversation

    val replyToMessage: StateFlow<MessageEntity?> = _replyToMessage

    val notSentMessages: StateFlow<List<MessageEntity>> = _notSentMessages

    val messageInputText: StateFlow<String> = _messageInputText

    val nextPageAvailable: StateFlow<Boolean> = _nextPageAvailable

    fun loadContacts(selectedChatId: String) {
        if (_contactNames.value is DatabaseRequestState.Loading
            || _contactNames.value is DatabaseRequestState.Success
        ) return

        _contactNames.value = DatabaseRequestState.Loading
        _selectedContact.value = DatabaseRequestState.Loading

        collectContacts(selectedChatId)
    }

    private fun collectContacts(selectedChatId: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.local.getContacts().collect { contacts ->
                    _contactNames.value = DatabaseRequestState.Success(
                        contacts.map { contact -> contact.name }
                    )

                    val selectedContact = contacts.find { item -> item.address == selectedChatId }

                    _selectedContact.value = if (selectedContact != null)
                        DatabaseRequestState.Success(selectedContact)
                    else
                        DatabaseRequestState.Error(Exception("Contact not found."))
                }
            } catch (ex: Exception) {
                _contactNames.value = DatabaseRequestState.Error(ex)
            }
        }
    }

    override fun validateNewContactName(name: String): Boolean {
        return name.isNotBlank() && try {
            (_contactNames.value as DatabaseRequestState.Success).data.all { item -> item != name }
        } catch (_: Exception) {
            false
        }
    }

    fun loadConversation(chatId: String) {
        if (_conversation.value is DatabaseRequestState.Success
            || _conversation.value is DatabaseRequestState.Loading
        ) return

        _conversation.value = DatabaseRequestState.Loading

        collectConversation(chatId)
        collectSearches(chatId)
    }

    private fun loadMessageReference(message: MessageEntity) {
        if (message.type.actionType == MessageActionType.REPLY) {
            if (message.type.refId != null) {
                message.responseFor = repository.local.getMessageByRefIdFlow(message.type.refId)
            }
        }
    }

    private fun initialConversationLoad(messages: List<MessageEntity>) {
        messages.forEach { item ->
            loadMessageReference(item)
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
        loadMessageReference(message)
        if (message.type.refId != null && message.type.actionType == MessageActionType.DELETE) {
            removeItemFromConversation(message.type.refId)
        } else if (message.type.actionType == MessageActionType.DELETE_ALL) {
            clearConversation()
        }
        _conversationSortedSet.add(message)
    }

    private fun setConversation() {
        _conversation.value = DatabaseRequestState.Success(
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
                _conversation.value = DatabaseRequestState.Error(ex)
            }
        }
    }

    private fun collectSearches(chatId: String) {
        viewModelScope.launch(ioDispatcher) {
            _filterQuery.collect { query ->
                _conversation.value = DatabaseRequestState.Loading
                try {
                    val searchResult =
                        repository.local.getConversation(chatId, getLastMessageId(), query)
                    synchronized(_conversationSortedSet) {
                        clearConversation()
                        initialConversationLoad(searchResult)
                        setConversation()
                    }
                } catch (ex: Exception) {
                    _conversation.value = DatabaseRequestState.Error(ex)
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
                _conversation.value = DatabaseRequestState.Error(ex)
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
            postToDatabase(MessageAction(MessageActionType.DELETE_ALL, null), "")
        }
    }

    fun removeMessages(messages: List<MessageEntity>) {
        synchronized(_conversationSortedSet) { _conversationSortedSet.removeAll(messages.toSet()) }
        viewModelScope.launch(ioDispatcher) {
            val textMessages =
                messages.filter { item -> item.type.actionType == MessageActionType.TEXT }
            val textMessagesWithRefs = textMessages.filter { item -> item.refId != null }
            val nonText = messages.filter { item -> item.type.actionType != MessageActionType.TEXT }
            repository.local.removeMessagesSoft(textMessages)
            repository.local.removeMessagesHard(nonText)
            textMessagesWithRefs.forEach { item ->
                postToDatabase(MessageAction(MessageActionType.DELETE, item.refId), "")
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

    private suspend fun postToDatabase(type: MessageAction, text: String): Boolean {
        return try {
            val contact = getSelectedContactEntity() ?: return false
            val message = MessageEntity(
                chatId = contact.address,
                text = text,
                incoming = false,
                sent = false,
                type = type,
                uuid = null
            )
            messageSaver.saveOutgoingMessage(message)
            true
        } catch (ex: Exception) {
            false
        }
    }

    private suspend fun postToDatabase(): Boolean {
        if (messageInputText.value.isBlank()) {
            return false
        }
        val type = if (replyToMessage.value != null)
            MessageAction(MessageActionType.REPLY, replyToMessage.value?.refId)
        else
            MessageAction(MessageActionType.TEXT, null)
        return postToDatabase(type, messageInputText.value)
    }

    private fun getSelectedContactEntity(): ContactEntity? {
        return try {
            (selectedContact.value as DatabaseRequestState.Success<ContactEntity>).data
        } catch (_: Exception) {
            return null
        }
    }

    private fun getLastMessageId(): Long {
        return try {
            (selectedContact.value as DatabaseRequestState.Success<ContactEntity>).data.lastMessageId
                ?: 1
        } catch (_: Exception) {
            1
        }
    }

    override fun getContactEntityForSaving(): ContactEntity? {
        return try {
            val contact = (selectedContact.value as DatabaseRequestState.Success).data
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
