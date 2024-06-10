package com.example.enigma.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.enigma.crypto.AddressProvider
import com.example.enigma.crypto.CryptoProvider
import com.example.enigma.data.MessageSaver
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.data.network.SignalRClient
import com.example.enigma.models.MessageBase
import com.example.enigma.models.MessageExtended
import com.example.enigma.routing.PathFinder
import com.example.enigma.util.AddressHelper
import com.example.enigma.util.DatabaseRequestState
import com.example.enigma.util.copyBySerialization
import com.example.enigma.util.isFullPage
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date
import java.util.SortedSet
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageSaver: MessageSaver,
    private val signalRClient: SignalRClient,
    private val addressProvider: AddressProvider,
    repository: Repository,
    application: Application,
) : BaseViewModel(repository, signalRClient, application) {

    private val _conversationSupportListMutex = Mutex(false)

    private val _conversationSupportListComparator = compareBy<MessageEntity> { item -> item.id }

    private val _conversationSupportList: SortedSet<MessageEntity>
            = sortedSetOf(_conversationSupportListComparator)

    private var _filterQuery = MutableStateFlow("")

    private val _contactNames =
        MutableStateFlow<DatabaseRequestState<List<String>>>(DatabaseRequestState.Idle)

    private val _selectedContact =
        MutableStateFlow<DatabaseRequestState<ContactEntity>>(DatabaseRequestState.Idle)

    private val _conversation =
        MutableStateFlow<DatabaseRequestState<List<MessageEntity>>>(DatabaseRequestState.Idle)

    private val _pathsExist =
        MutableStateFlow<DatabaseRequestState<Boolean>>(DatabaseRequestState.Idle)

    private val _nextPageAvailable = MutableStateFlow(false)

    private val _messageInputText = MutableStateFlow("")

    val selectedContact: StateFlow<DatabaseRequestState<ContactEntity>> = _selectedContact

    val conversation: StateFlow<DatabaseRequestState<List<MessageEntity>>> = _conversation

    val pathsExist: StateFlow<DatabaseRequestState<Boolean>> = _pathsExist

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
        return super.validateNewContactName(name) && try {
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
                    _conversationSupportListMutex.withLock {
                        if (_conversationSupportList.isEmpty()) {
                            _nextPageAvailable.value = messages.isFullPage()
                        }
                        _conversationSupportList.addAll(
                            messages.filter { item -> item.text.contains(_filterQuery.value) }
                        )
                        _conversation.value = DatabaseRequestState.Success(
                            _conversationSupportList.toList()
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
                _conversationSupportListMutex.withLock {
                    try {
                        val searchResult = repository.local.getConversation(
                            chatId,
                            getLastMessageId() - 1,
                            query
                        )
                        _conversationSupportList.clear()
                        _conversationSupportList.addAll(searchResult)
                        _conversation.value = DatabaseRequestState.Success(
                            _conversationSupportList.toList()
                        )
                        _nextPageAvailable.value = searchResult.isFullPage()
                    } catch (ex: Exception) {
                        _conversation.value = DatabaseRequestState.Error(ex)
                    }
                }
            }
        }
    }

    fun loadNextPage(chatId: String)
    {
        viewModelScope.launch(ioDispatcher) {
            _conversationSupportListMutex.withLock {
                try {
                    val lastIndex = _conversationSupportList.last().id
                    val nextPage = repository.local.getConversation(chatId, lastIndex, _filterQuery.value)

                    if (nextPage.isEmpty()) {
                        _nextPageAvailable.value = false
                    } else {
                        _conversationSupportList.addAll(nextPage)
                        _conversation.value = DatabaseRequestState.Success(
                            _conversationSupportList.toList()
                        )
                        _nextPageAvailable.value = nextPage.isFullPage()
                    }
                } catch (ex: Exception) {
                    _conversation.value = DatabaseRequestState.Error(ex)
                }
            }
        }
    }

    fun searchConversation(searchQuery: String) {
        _filterQuery.update { searchQuery }
    }

    fun checkPathExistence(chatId: String) {
        if(_pathsExist.value is DatabaseRequestState.Success
            || _pathsExist.value is DatabaseRequestState.Loading) return

        _pathsExist.value = DatabaseRequestState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.local.graphPathExists(chatId).collect { exists ->
                    _pathsExist.value = DatabaseRequestState.Success(exists)
                }
            } catch (ex: Exception) {
                _pathsExist.value = DatabaseRequestState.Error(ex)
            }
        }
    }

    fun markConversationAsRead(chatId: String) {
        viewModelScope.launch(ioDispatcher) {
            repository.local.markConversationAsRead(chatId)
        }
    }

    fun clearConversation(chatId: String)
    {
        viewModelScope.launch(ioDispatcher) {
            _conversationSupportListMutex.withLock {
                _conversationSupportList.clear()
                repository.local.clearConversation(chatId)
            }
        }
    }

    fun removeMessages(messages: List<MessageEntity>)
    {
        viewModelScope.launch(ioDispatcher) {
            _conversationSupportListMutex.withLock {
                _conversationSupportList.removeAll(messages.toSet())
                repository.local.removeMessages(messages, _conversationSupportList.firstOrNull()?.id)
            }
        }
    }

    fun calculateCircuit() {
        viewModelScope.launch(ioDispatcher) {
            try {
                val pathFinder = PathFinder(repository)
                if(!pathFinder.load() ||
                    selectedContact.value !is DatabaseRequestState.Success)
                {
                    return@launch
                }

                pathFinder.calculatePaths(
                    (selectedContact.value as DatabaseRequestState.Success).data
                )
            } catch (_: Exception) {
                // TODO: inform user about error
            }
        }
    }

    fun sendMessage() {
        if (!signalRClient.isConnected() || messageInputText.value.isEmpty()) {
            return
        }

        viewModelScope.launch(ioDispatcher) {
            if (!dispatchMessage()) {
                return@launch
            }

            if (saveMessageToDatabase()) {
                _messageInputText.value = ""
            }
        }
    }

    private suspend fun dispatchMessage(): Boolean {
        if (!signalRClient.isConnected()) {
            return false
        }

        val path = chosePath() ?: return false
        val onion = buildOnion(path) ?: return false

        return signalRClient.sendMessage(onion)
    }

    private suspend fun saveMessageToDatabase(): Boolean {
        val contact = getSelectedContactEntity() ?: return false
        val message = MessageEntity(contact.address, messageInputText.value, false, Date())

        messageSaver.saveOutgoingMessage(message)

        return true
    }

    private suspend fun chosePath(): List<String>? {
        val contact = getSelectedContactEntity() ?: return null
        val paths = repository.local.getGraphPath(contact.address)

        if (paths.isEmpty()) {
            return null
        }

        val path = paths[Random.nextInt(0, paths.size)].path

        if (path.size < 2) {
            return null
        }

        return path
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

    private fun getMessageEntities(): List<MessageEntity> {
        return try {
            (conversation.value as DatabaseRequestState.Success<List<MessageEntity>>).data
        }
        catch (_: Exception)
        {
            return listOf()
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

    private fun publicKeyRequiredIntoOnion(): Boolean
    {
        val messageEntities = getMessageEntities()

        return messageEntities.isEmpty() || !messageEntities.any { item -> item.incoming }
    }

    private suspend fun buildOnion(path: List<String>): String?
    {
        val guard = repository.local.getGuard() ?: return null
        val localAddress = addressProvider.address ?: return null
        val addresses = arrayOf(localAddress) + path.subList(0, path.size - 1)
            .map { item -> AddressHelper.getHexAddressFromPublicKey(item) }

        val json = Gson()
        val message = if (publicKeyRequiredIntoOnion()) json.toJson(
            MessageExtended(
                messageInputText.value,
                if (addressProvider.publicKey != null) addressProvider.publicKey!! else "",
                guard.hostname
            )
        ) else json.toJson(MessageBase(messageInputText.value))

        return CryptoProvider.buildOnion(message.toByteArray(), path.toTypedArray(), addresses)
    }

    override fun createContactEntityForSaving(): ContactEntity? {
        return try {
            val newContact = copyBySerialization(
                (selectedContact.value as DatabaseRequestState.Success).data
            )
            newContact.name = newContactName.value

            newContact
        } catch (_: Exception) {
            null
        }
    }

    fun setMessageInputText(text: String)
    {
        _messageInputText.value = text
    }

    fun resetSearchQuery()
    {
        searchConversation("")
    }

    override fun reset()
    {
        super.reset()
        resetSearchQuery()
    }
}
