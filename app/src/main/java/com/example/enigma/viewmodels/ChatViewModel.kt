package com.example.enigma.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.routing.PathFinder
import com.example.enigma.util.Constants.Companion.SELECTED_CHAT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
    private val pathFinder: PathFinder,
    application: Application
) : AndroidViewModel(application){

    val chatId: String

    init {
        val selectedChatId = savedStateHandle.get<String>(SELECTED_CHAT_ID)
        chatId = selectedChatId ?: "not-found"
    }

    val contact: LiveData<ContactEntity?> = repository.local.getContact(chatId).asLiveData()

    val conversation: LiveData<List<MessageEntity>> = repository.local.getConversation(chatId).asLiveData()

    val pathsExists: LiveData<Boolean> = repository.local.graphPathExists(chatId).asLiveData()

    val errorCalculatingPath: MutableLiveData<Boolean> = MutableLiveData(false)

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
            if(contact.value != null)
            {
                if(pathFinder.load())
                {
                    val pathCalculationResult = pathFinder.calculatePaths(contact.value!!)
                    errorCalculatingPath.postValue(pathCalculationResult)
                }
            } else {
                errorCalculatingPath.postValue(false)
            }
        }
    }
}
