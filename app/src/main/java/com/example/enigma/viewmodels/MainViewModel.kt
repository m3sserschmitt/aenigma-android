package com.example.enigma.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.enigma.crypto.AddressProvider
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.network.SignalRClient
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
    private val addressProvider: AddressProvider,
    application: Application,
    signalRClient: SignalRClient
) : BaseViewModel(application, signalRClient) {

    val readContacts: LiveData<List<ContactEntity>> get() = repository.local.getContacts().asLiveData()

    val guardAvailable: LiveData<Boolean> get() = repository.local.isGuardAvailable().asLiveData()

    private fun generateQrCodeBitmap(): Flow<Bitmap?>
    {
        return flow {
            val guard = repository.local.getGuard()

            if (guard != null && addressProvider.address != null) {
                val exportedData = ExportedContactData(
                    guard.hostname,
                    addressProvider.publicKey!!
                )

                val bitmap = QrCodeGenerator(400, 400)
                    .encodeAsBitmap(exportedData.toString())

                if (bitmap != null) {
                    emit(bitmap)
                }
            } else {
                emit(null)
            }
        }
    }

    val qrCode: LiveData<Bitmap?> = generateQrCodeBitmap().asLiveData()

    fun insertContact(contact: ContactEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertContact(contact)
        }
}
