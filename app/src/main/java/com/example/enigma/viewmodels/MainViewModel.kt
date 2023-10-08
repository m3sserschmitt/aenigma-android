package com.example.enigma.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.util.Constants.Companion.SERVER_ADDRESS
import com.example.enigma.util.ExportedContactData
import com.example.enigma.util.QrCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    val readContacts: LiveData<List<ContactEntity>> = repository.local.getContacts().asLiveData()

    val keysAvailable: LiveData<Boolean> = repository.local.isKeyAvailable().asLiveData()

    val qrCodeBitmap: LiveData<Bitmap> = flow {
        repository.local.getPublicKey().collect {
            val exportedData = ExportedContactData(SERVER_ADDRESS, it)
            val bitmap = QrCodeGenerator(400, 400)
                .encodeAsBitmap(exportedData.toString())

            if (bitmap != null) {
                emit(bitmap)
            }
        }
    }.asLiveData()

    fun insertContact(contact: ContactEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertContact(contact)
        }
}
