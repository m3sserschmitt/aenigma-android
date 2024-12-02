package com.example.enigma.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.enigma.data.MessageSaver
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.data.network.SignalRClient
import com.example.enigma.util.DatabaseRequestState
import com.example.enigma.util.isFullPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    private val _conversationSortedSet: SortedSet<MessageEntity>
            = sortedSetOf(_conversationSupportListComparator)

    private var _filterQuery = MutableStateFlow("")

    private val _contactNames =
        MutableStateFlow<DatabaseRequestState<List<String>>>(DatabaseRequestState.Idle)

    private val _selectedContact =
        MutableStateFlow<DatabaseRequestState<ContactEntity>>(DatabaseRequestState.Idle)

    private val _conversation =
        MutableStateFlow<DatabaseRequestState<List<MessageEntity>>>(DatabaseRequestState.Idle)

    private val _notSentMessages = MutableStateFlow<List<MessageEntity>>(listOf())

    private val _nextPageAvailable = MutableStateFlow(false)

    private val _messageInputText = MutableStateFlow("")

    val selectedContact: StateFlow<DatabaseRequestState<ContactEntity>> = _selectedContact

    val conversation: StateFlow<DatabaseRequestState<List<MessageEntity>>> = _conversation

    val notSentMessages: StateFlow<List<MessageEntity>> = _notSentMessages

    val messageInputText: StateFlow<String> = _messageInputText

    val nextPageAvailable: StateFlow<Boolean> = _nextPageAvailable

    fun loadContacts(selectedChatId: String) {
        if(_contactNames.value is DatabaseRequestState.Loading
            || _contactNames.value is DatabaseRequestState.Success) return

        _contactNames.value = DatabaseRequestState.Loading
        _selectedContact.value = DatabaseRequestState.Loading

        collectContacts(selectedChatId)
    }

    private fun collectContacts(selectedChatId: String)
    {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.local.getContacts().collect { contacts ->
                    _contactNames.value = DatabaseRequestState.Success(
                        contacts.map { contact -> contact.name }
                    )

                    val selectedContact = contacts.find { item -> item.address == selectedChatId }

                    _selectedContact.value = if(selectedContact != null)
                        DatabaseRequestState.Success(selectedContact)
                    else
                        DatabaseRequestState.Error(Exception("Contact not found."))
                }
            }
            catch (ex: Exception)
            {
                _contactNames.value = DatabaseRequestState.Error(ex)
            }
        }
    }

    override fun validateNewContactName(name: String): Boolean {
        return name.isNotBlank() && try {
            (_contactNames.value as DatabaseRequestState.Success).data.all { item -> item != name }
        }
        catch (_: Exception)
        {
            false
        }
    }

    fun loadConversation(chatId: String) {
        if(_conversation.value is DatabaseRequestState.Success
            || _conversation.value is DatabaseRequestState.Loading) return

        _conversation.value = DatabaseRequestState.Loading

        collectConversation(chatId)
        collectSearches(chatId)
    }

    private fun collectConversation(chatId: String)
    {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.local.getConversation(chatId).collect { messages ->
                    synchronized(_conversationSortedSet) {
                        if (_conversationSortedSet.isEmpty()) {
                            _nextPageAvailable.value = messages.isFullPage()
                        }
                        _conversationSortedSet.addAll(
                            messages.filter { item -> item.text.contains(_filterQuery.value) }
                        )
                        _notSentMessages.value = messages.filter { item -> !item.sent }
                        _conversation.value = DatabaseRequestState.Success(
                            _conversationSortedSet.toList()
                        )
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
                    val searchResult = repository.local.getConversation(chatId, getLastMessageId() - 1, query)
                    synchronized(_conversationSortedSet) {
                        _conversationSortedSet.clear()
                        _conversationSortedSet.addAll(searchResult)
                        _conversation.value = DatabaseRequestState.Success(
                            _conversationSortedSet.toList()
                        )
                        _nextPageAvailable.value = searchResult.isFullPage()
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
                val nextPage = repository.local.getConversation(chatId, lastIndex, _filterQuery.value)
                synchronized(_conversationSortedSet) {
                    if (nextPage.isEmpty()) {
                        _nextPageAvailable.value = false
                    } else {
                        _conversationSortedSet.addAll(nextPage)
                        _conversation.value = DatabaseRequestState.Success(
                            _conversationSortedSet.toList()
                        )
                        _nextPageAvailable.value = nextPage.isFullPage()
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

    fun clearConversation(chatId: String)
    {
        synchronized(_conversationSortedSet) { _conversationSortedSet.clear() }
        viewModelScope.launch(ioDispatcher) { repository.local.clearConversation(chatId) }
    }

    fun removeMessages(messages: List<MessageEntity>)
    {
        synchronized(_conversationSortedSet) { _conversationSortedSet.removeAll(messages.toSet()) }
        viewModelScope.launch(ioDispatcher) {
            repository.local.removeMessages(messages, _conversationSortedSet.firstOrNull()?.id)
        }
    }

    fun sendMessage() {
        viewModelScope.launch(ioDispatcher) {
            if (saveMessageToDatabase()) {
                _messageInputText.value = ""
            }
        }
    }

    private suspend fun saveMessageToDatabase(): Boolean {
        if(messageInputText.value.isBlank())
        {
            return false
        }
        val contact = getSelectedContactEntity() ?: return false
        val message = MessageEntity(
            contact.address,
            messageInputText.value,
            incoming = false,
            sent = false)

        messageSaver.saveOutgoingMessage(message)

        return true
    }

    private fun getSelectedContactEntity(): ContactEntity? {
        return try {
            (selectedContact.value as DatabaseRequestState.Success<ContactEntity>).data
        }
        catch (_: Exception)
        {
            return null
        }
    }

    private fun getLastMessageId(): Long
    {
        return try {
            (selectedContact.value as DatabaseRequestState.Success<ContactEntity>).data.lastMessageId ?: 1
        }
        catch (_: Exception)
        {
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

    fun setMessageInputText(text: String)
    {
        _messageInputText.value = text
    }

    private fun resetSearchQuery()
    {
        searchConversation("")
    }

    override fun resetContactChanges() {
        resetNewContactName()
    }

    override fun init()
    {
        resetSearchQuery()
        resetNewContactName()
    }
}
