package com.example.enigma.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.GuardEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.data.network.MessageDispatcher
import com.example.enigma.util.Constants.Companion.SELECTED_CHAT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: Repository,
    private var messageDispatcher: MessageDispatcher,
    private val savedStateHandle: SavedStateHandle,
    application: Application
) : AndroidViewModel(application){

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

    fun markConversationAsRead()
    {
        viewModelScope.launch(Dispatchers.IO) {
            savedStateHandle.get<String>(SELECTED_CHAT_ID)
                ?.let {
                    repository.local.markConversationAsRead(it)
                }
        }
    }

    fun dispatchMessage(text: String)
    {

    }
}
