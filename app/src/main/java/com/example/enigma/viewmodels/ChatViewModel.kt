package com.example.enigma.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.enigma.crypto.AddressProvider
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.GuardEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.routing.PathFinder
import com.example.enigma.util.Constants.Companion.SELECTED_CHAT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
    private val pathFinder: PathFinder,
    addressProvider: AddressProvider,
    application: Application
) : AndroidViewModel(application){

    val chatId: String? = savedStateHandle.get<String>(SELECTED_CHAT_ID)

    private fun getContact() : Flow<ContactEntity>?
    {
        return savedStateHandle.get<String>(SELECTED_CHAT_ID)?.let {
            repository.local.getContact(it) }
    }

    val contact: LiveData<ContactEntity>? = getContact()?.asLiveData()

    private fun readConversation() : Flow<List<MessageEntity>>?
    {
        return savedStateHandle.get<String>(SELECTED_CHAT_ID)
            ?.let { repository.local.getConversation(it) }
    }

    val conversation: LiveData<List<MessageEntity>>? = readConversation()?.asLiveData()

    private fun getGuard(): Flow<GuardEntity> = repository.local.getGuard()

    val guard: LiveData<GuardEntity> = getGuard().asLiveData()

    private fun checkIfPathsExists(): Flow<Boolean>? = savedStateHandle.get<String>(SELECTED_CHAT_ID)
        ?.let { repository.local.graphPathExists(it) }

    val pathsExists: LiveData<Boolean>? = checkIfPathsExists()?.asLiveData()

    val localAddress: String? = addressProvider.address

    val graphLoaded: LiveData<Boolean> = pathFinder.loaded

    fun markConversationAsRead()
    {
        viewModelScope.launch(Dispatchers.IO) {
            savedStateHandle.get<String>(SELECTED_CHAT_ID)
                ?.let {
                    repository.local.markConversationAsRead(it)
                }
        }
    }

    fun calculatePath()
    {
        viewModelScope.launch(Dispatchers.IO) {
            pathFinder.calculatePaths(contact?.value!!)
        }
    }

    fun loadGraph()
    {
        viewModelScope.launch {
            pathFinder.load()
        }
    }
}
