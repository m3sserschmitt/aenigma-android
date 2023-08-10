package com.example.enigma.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    val readContacts: LiveData<List<ContactEntity>> = repository.local.getContacts().asLiveData()

    fun insertContact(contact: ContactEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertContact(contact)
        }
}
