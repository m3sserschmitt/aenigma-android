package com.example.enigma.viewmodels

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.network.SignalRClient
import com.example.enigma.data.network.SignalRStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

abstract class BaseViewModel(
    protected val repository: Repository,
    private val signalRClient: SignalRClient,
    application: Application,
): AndroidViewModel(application) {

    protected var ioDispatcher = Dispatchers.IO

    protected var defaultDispatcher = Dispatchers.Default

    val signalRClientStatus: LiveData<SignalRStatus> = signalRClient.status

    val newContactName: MutableState<String> = mutableStateOf("")

    abstract fun createContactEntityForSaving(): ContactEntity?

    abstract fun resetNewContactDetails()

    abstract fun checkIfContactNameExists(name: String): Boolean

    fun saveNewContact()
    {
        viewModelScope.launch(ioDispatcher) {
            val contact = createContactEntityForSaving() ?: return@launch
            resetNewContactDetails()
            repository.local.insertOrUpdateContact(contact)
        }
    }

    fun updateNewContactName(newValue: String): Boolean
    {
        newContactName.value = newValue
        try {

            if(newValue.isEmpty())
            {
                return false
            }

            return checkIfContactNameExists(newValue)
        }
        catch (_: Exception)
        {
            return false
        }
    }

    fun resetClientStatus()
    {
        signalRClient.resetStatus()
    }
}
