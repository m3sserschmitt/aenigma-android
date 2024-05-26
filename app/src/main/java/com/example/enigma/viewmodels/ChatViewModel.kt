package com.example.enigma.viewmodels

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
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

    private val _selectedContact =
        MutableStateFlow<DatabaseRequestState<ContactEntity>>(DatabaseRequestState.Idle)

    private val _conversationSupportListComparator = compareBy<MessageEntity> { item -> item.id }

    private val _conversationSupportList: SortedSet<MessageEntity>
    = sortedSetOf(_conversationSupportListComparator)

    private val _conversation =
        MutableStateFlow<DatabaseRequestState<List<MessageEntity>>>(DatabaseRequestState.Idle)

    private val _pathsExist =
        MutableStateFlow<DatabaseRequestState<Boolean>>(DatabaseRequestState.Idle)

    private val _searchedMessages =
        MutableStateFlow<DatabaseRequestState<List<MessageEntity>>>(DatabaseRequestState.Idle)

    val selectedContact: StateFlow<DatabaseRequestState<ContactEntity>> = _selectedContact

    val conversation: StateFlow<DatabaseRequestState<List<MessageEntity>>> = _conversation

    val pathsExist: MutableStateFlow<DatabaseRequestState<Boolean>> = _pathsExist

    val searchedMessages: StateFlow<DatabaseRequestState<List<MessageEntity>>> = _searchedMessages

    val messageInputText: MutableState<String> = mutableStateOf("")

    val nextPageAvailable: MutableState<Boolean> = mutableStateOf(false)

    fun loadContact(chatId: String) {
        if(_selectedContact.value is DatabaseRequestState.Success
            || _selectedContact.value is DatabaseRequestState.Loading) return

        viewModelScope.launch {
            _selectedContact.value = DatabaseRequestState.Loading
            try {
                repository.local.getContactFlow(chatId).collect { contact ->
                    _selectedContact.value =
                        if (contact != null) DatabaseRequestState.Success(contact)
                        else DatabaseRequestState.Error(Exception("Contact not found."))
                }
            } catch (ex: Exception) {
                _selectedContact.value = DatabaseRequestState.Error(ex)
            }
        }
    }

    fun loadConversation(chatId: String) {
        if(_conversation.value is DatabaseRequestState.Success
            || _conversation.value is DatabaseRequestState.Loading) return

        viewModelScope.launch(Dispatchers.IO) {
            _conversation.value = DatabaseRequestState.Loading
            try {
                repository.local.getConversation(chatId).collect { messages ->
                    if(_conversationSupportList.isEmpty()) {
                        nextPageAvailable.value = messages.isFullPage()
                    }
                    _conversationSupportList.addAll(messages)
                    _conversation.value = DatabaseRequestState.Success(
                        _conversationSupportList.toList()
                    )
                }
            } catch (ex: Exception) {
                _conversation.value = DatabaseRequestState.Error(ex)
            }
        }
    }

    fun loadNextPage(chatId: String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lastIndex = _conversationSupportList.last().id
                val nextPage = repository.local.getConversation(chatId, lastIndex)

                if (nextPage.isEmpty())
                {
                    nextPageAvailable.value = false
                }
                else
                {
                    _conversationSupportList.addAll(nextPage)
                    _conversation.value = DatabaseRequestState.Success(
                        _conversationSupportList.toList()
                    )
                    nextPageAvailable.value = nextPage.isFullPage()
                }
            } catch (ex: Exception) {
                _conversation.value = DatabaseRequestState.Error(ex)
            }
        }
    }

    fun searchConversation(chatId: String, searchQuery: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchedMessages.value = DatabaseRequestState.Loading
            try {
                _searchedMessages.value = DatabaseRequestState.Success(
                    repository.local.searchConversation(chatId, searchQuery)
                )
            } catch (ex: Exception) {
                _searchedMessages.value = DatabaseRequestState.Error(ex)
            }
        }
    }

    fun checkPathExistence(chatId: String) {
        if(_pathsExist.value is DatabaseRequestState.Success
            || _pathsExist.value is DatabaseRequestState.Loading) return

        viewModelScope.launch {
            _pathsExist.value = DatabaseRequestState.Loading
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
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.markConversationAsRead(chatId)
        }
    }

    fun clearConversation(chatId: String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.clearConversation(chatId)
        }
    }

    fun removeMessages(messages: List<MessageEntity>)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.removeMessages(messages)
        }
    }

    fun calculateCircuit() {
        viewModelScope.launch(Dispatchers.IO) {
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

        viewModelScope.launch(Dispatchers.IO) {
            if (!dispatchMessage()) {
                return@launch
            }

            if (saveMessageToDatabase()) {
                messageInputText.value = ""
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
        if (selectedContact.value !is DatabaseRequestState.Success) {
            return null
        }

        return (selectedContact.value as DatabaseRequestState.Success<ContactEntity>).data
    }

    private fun getMessageEntities(): List<MessageEntity> {
        if (conversation.value !is DatabaseRequestState.Success) {
            return listOf()
        }

        return (conversation.value as DatabaseRequestState.Success<List<MessageEntity>>).data
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

    override fun resetNewContactDetails() {
        newContactName.value = ""
    }
}
