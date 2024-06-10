package com.example.enigma.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.network.SignalRClient
import com.example.enigma.data.network.SignalRStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

abstract class BaseViewModel(
    protected val repository: Repository,
    private val signalRClient: SignalRClient,
    application: Application,
): AndroidViewModel(application) {

    private val _newContactName: MutableStateFlow<String> = MutableStateFlow("")

    protected var ioDispatcher = Dispatchers.IO

    protected var defaultDispatcher = Dispatchers.Default

    val signalRClientStatus: LiveData<SignalRStatus> = signalRClient.status

    val newContactName: StateFlow<String> = _newContactName

    abstract fun createContactEntityForSaving(): ContactEntity?

    open fun resetNewContactDetails()
    {
        resetNewContactName()
    }

    open fun reset()
    {
        resetNewContactDetails()
    }

    fun resetNewContactName()
    {
        setNewContactName("")
    }

    open fun validateNewContactName(name: String): Boolean
    {
        return name.isNotBlank()
    }

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
        _newContactName.value = newValue
        try {

            if(newValue.isEmpty())
            {
                return false
            }

            return validateNewContactName(newValue)
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

    fun setNewContactName(name: String)
    {
        _newContactName.value = name
    }
}
