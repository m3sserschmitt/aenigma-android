package com.example.enigma.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.util.Constants.Companion.SELECTED_CHAT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
    application: Application
) : AndroidViewModel(application){

    fun getContact() : LiveData<ContactEntity>? =
        savedStateHandle.get<String>(SELECTED_CHAT_ID)?.let {
            repository.local.getContact(it).asLiveData() }

    fun readConversation() : LiveData<List<MessageEntity>>? =
        savedStateHandle.get<String>(SELECTED_CHAT_ID)
            ?.let { repository.local.getConversation(it).asLiveData() }

    fun insertMessage(messageEntity: MessageEntity)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertMessage(messageEntity)
        }
    }

    fun insertOutgoingMessage(text: String)
    {
        savedStateHandle.get<String>(SELECTED_CHAT_ID)?.let{
            insertMessage(MessageEntity(it, text, false, Date()))
        }
    }
}
