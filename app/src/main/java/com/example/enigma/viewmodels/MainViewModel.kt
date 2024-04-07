package com.example.enigma.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.enigma.crypto.AddressProvider
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.network.SignalRClient
import com.example.enigma.util.DatabaseRequestState
import com.example.enigma.models.ExportedContactData
import com.example.enigma.util.AddressHelper
import com.example.enigma.util.QrCodeGenerator
import com.example.enigma.util.SearchAppBarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: Repository,
    private val addressProvider: AddressProvider,
    application: Application,
    signalRClient: SignalRClient
) : BaseViewModel(repository, application, signalRClient) {

    val searchAppBarState: MutableState<SearchAppBarState> =
        mutableStateOf(SearchAppBarState.CLOSED)

    val contactsSearch: MutableState<String> = mutableStateOf("")

    val scannedContactDetails: MutableState<ExportedContactData>
    = mutableStateOf(ExportedContactData("", ""))

    val allContacts: StateFlow<DatabaseRequestState<List<ContactEntity>>> = _allContacts

    private val _contactQrCode
    = MutableStateFlow<DatabaseRequestState<Bitmap>>(DatabaseRequestState.Idle)

    val contactQrCode: StateFlow<DatabaseRequestState<Bitmap>> = _contactQrCode

    fun generateCode()
    {
        viewModelScope.launch {
            _contactQrCode.value = DatabaseRequestState.Loading
            try {
               generateQrCodeBitmap().collect {
                   qrCode -> if(qrCode != null)
                       _contactQrCode.value = DatabaseRequestState.Success(qrCode)
                   else
                       _contactQrCode.value = DatabaseRequestState.Error(
                           Exception("Failed to generate contact QR Code")
                       )
               }
            } catch (ex: Exception)
            {
                _contactQrCode.value = DatabaseRequestState.Error(ex)
            }
        }
    }

    override fun createContactEntityForSaving(): ContactEntity {
        val contactAddress = AddressHelper.getHexAddressFromPublicKey(scannedContactDetails.value.publicKey)

        return ContactEntity(
            contactAddress,
            newContactName.value,
            scannedContactDetails.value.publicKey,
            scannedContactDetails.value.guardHostname,
            false
        )
    }

    override fun resetNewContactDetails() {
        newContactName.value = ""
        scannedContactDetails.value = ExportedContactData("", "")
    }

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
}
