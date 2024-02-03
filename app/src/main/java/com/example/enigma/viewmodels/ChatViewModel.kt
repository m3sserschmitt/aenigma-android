package com.example.enigma.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.data.network.SignalRClient
import com.example.enigma.routing.PathFinder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: Repository,
    private val pathFinder: PathFinder,
    application: Application,
    signalRClient: SignalRClient
) : BaseViewModel(application, signalRClient){

    private var chatId: String? = null

    lateinit var contact: LiveData<ContactEntity?>

    lateinit var conversation: LiveData<List<MessageEntity>>

    lateinit var pathsExists: LiveData<Boolean>

    val errorCalculatingPath: MutableLiveData<Boolean> = MutableLiveData(false)

    var test: LiveData<String> = MutableLiveData("Test")

    fun load(contactId: String)
    {
        chatId = contactId
        contact = repository.local.getContact(contactId).asLiveData()
        conversation = repository.local.getConversation(contactId).asLiveData()
        pathsExists = repository.local.graphPathExists(contactId).asLiveData()
    }

    fun markConversationAsRead()
    {
        viewModelScope.launch(Dispatchers.IO) {
            chatId?.let {
                    repository.local.markConversationAsRead(it)
                }
        }
    }

    fun calculatePath()
    {
        viewModelScope.launch(Dispatchers.IO) {
            chatId?.let { id ->
                repository.local.getContact(id).collect { contact ->
                    if(contact != null && pathFinder.load()) {
                        val pathCalculationResult = pathFinder.calculatePaths(contact)
                        errorCalculatingPath.postValue(pathCalculationResult)
                    } else {
                        errorCalculatingPath.postValue(false)
                    }
                }
            }
        }
    }
}
