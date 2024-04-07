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
import com.example.enigma.util.DatabaseRequestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

abstract class BaseViewModel(
    val repository: Repository,
    application: Application,
    signalRClient: SignalRClient
): AndroidViewModel(application) {

    protected val _allContacts =
        MutableStateFlow<DatabaseRequestState<List<ContactEntity>>>(DatabaseRequestState.Idle)

    val signalRClientStatus: LiveData<SignalRStatus> = signalRClient.status

    val newContactName: MutableState<String> = mutableStateOf("")

    abstract fun createContactEntityForSaving(): ContactEntity?

    abstract fun resetNewContactDetails()

    fun saveNewContact()
    {
        viewModelScope.launch(Dispatchers.IO) {
            val contact = createContactEntityForSaving() ?: return@launch
            resetNewContactDetails()
            repository.local.insertOrUpdateContact(contact)
        }
    }

    fun loadContacts()
    {
        viewModelScope.launch {
            _allContacts.value = DatabaseRequestState.Loading
            try {
                repository.local.getContacts().collect {
                        contacts -> _allContacts.value = DatabaseRequestState.Success(contacts)
                }
            } catch (ex: Exception)
            {
                _allContacts.value = DatabaseRequestState.Error(ex)
            }
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

            if (_allContacts.value is DatabaseRequestState.Success) {
                return (_allContacts.value as DatabaseRequestState.Success)
                    .data
                    .all { item -> item.name != newValue }
            }

            return false
        }
        catch (_: Exception)
        {
            return false
        }
    }
}
