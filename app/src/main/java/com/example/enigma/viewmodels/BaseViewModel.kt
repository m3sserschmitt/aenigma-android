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

abstract class BaseViewModel(
    val repository: Repository,
    application: Application,
    signalRClient: SignalRClient
): AndroidViewModel(application) {

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
}
