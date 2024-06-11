package com.example.enigma.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.enigma.crypto.AddressProvider
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.ContactWithConversationPreview
import com.example.enigma.data.network.SignalRClient
import com.example.enigma.util.DatabaseRequestState
import com.example.enigma.models.ExportedContactData
import com.example.enigma.util.AddressHelper
import com.example.enigma.util.QrCodeGenerator
import com.example.enigma.util.copyBySerialization
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val addressProvider: AddressProvider,
    repository: Repository,
    application: Application,
    signalRClient: SignalRClient
) : BaseViewModel(
    repository,
    signalRClient,
    application) {

    private val _contactsSearchQuery: MutableStateFlow<String> = MutableStateFlow("")

    private val _allContacts =
        MutableStateFlow<DatabaseRequestState<List<ContactWithConversationPreview>>>(DatabaseRequestState.Idle)

    private val _contactQrCode
            = MutableStateFlow<DatabaseRequestState<Bitmap>>(DatabaseRequestState.Idle)

    private val _scannedContactDetails = MutableStateFlow(ExportedContactData("", ""))

    val allContacts: StateFlow<DatabaseRequestState<List<ContactWithConversationPreview>>> = _allContacts

    val contactQrCode: StateFlow<DatabaseRequestState<Bitmap>> = _contactQrCode

    val notificationsPermissionGranted: Flow<Boolean> = repository.local.notificationsAllowed

    val guardAvailable: LiveData<Boolean> get() = repository.local.isGuardAvailable().asLiveData()

    fun loadContacts()
    {
        if(_allContacts.value is DatabaseRequestState.Success
            || _allContacts.value is DatabaseRequestState.Loading) return

        _allContacts.value = DatabaseRequestState.Loading
        collectContacts()
        collectSearches()
    }

    private fun collectContacts()
    {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.local.getContactsWithConversationPreviewFlow().collect { contacts ->
                    val query = _contactsSearchQuery.value
                    val result = if(query.isNotBlank())
                        contacts.filter { contact -> contact.name.contains(query) }
                    else
                        contacts

                    _allContacts.value = DatabaseRequestState.Success(result)
                }
            }
            catch (ex: Exception)
            {
                _allContacts.value = DatabaseRequestState.Error(ex)
            }
        }
    }

    private fun collectSearches()
    {
        viewModelScope.launch(defaultDispatcher) {
            _contactsSearchQuery.collect { query ->
                _allContacts.value = DatabaseRequestState.Loading
                try {
                    val searchResult = if(query.isBlank())
                        repository.local.getContactsWithConversationPreview()
                    else
                        repository.local.searchContacts(query).map {
                            item -> item.toContactWithPreview()
                        }
                    _allContacts.value = DatabaseRequestState.Success(searchResult)
                }
                catch (ex: Exception)
                {
                    _allContacts.value = DatabaseRequestState.Error(ex)
                }
            }
        }
    }

    fun searchContacts(searchQuery: String)
    {
        _contactsSearchQuery.update { searchQuery }
    }

    fun resetSearchQuery()
    {
        searchContacts("")
    }

    fun generateCode()
    {
        if(_contactQrCode.value is DatabaseRequestState.Loading) return
        _contactQrCode.value = DatabaseRequestState.Loading
        viewModelScope.launch(ioDispatcher) {
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
        val contactAddress = AddressHelper.getHexAddressFromPublicKey(_scannedContactDetails.value.publicKey)

        return ContactEntity(
            contactAddress,
            newContactName.value,
            _scannedContactDetails.value.publicKey,
            _scannedContactDetails.value.guardHostname,
            false
        )
    }

    override fun resetNewContactDetails() {
        super.resetNewContactDetails()
        resetScannedContactDetails()
    }

    override fun validateNewContactName(name: String): Boolean {
        return super.validateNewContactName(name) && try {
            (_allContacts.value as DatabaseRequestState.Success).data.all { item ->
                item.name != name
            }
        } catch (_: Exception) {
            false
        }
    }

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

    fun deleteContacts(contacts: List<ContactWithConversationPreview>)
    {
        viewModelScope.launch(ioDispatcher) {
            repository.local.removeContacts(contacts.map { contact -> contact.toContact() })
        }
    }

    fun renameContact(contact: ContactWithConversationPreview)
    {
        viewModelScope.launch(ioDispatcher) {
            val updatedContact = copyBySerialization(contact)
            updatedContact.name = newContactName.value
            repository.local.updateContact(updatedContact.toContact())
        }
    }

    fun saveNotificationsPreference(granted: Boolean)
    {
        viewModelScope.launch(ioDispatcher) {
            repository.local.saveNotificationsAllowed(granted)
        }
    }

    fun setScannedContactDetails(scannedDetails: String): Boolean
    {
        return try {
            _scannedContactDetails.value = Gson().fromJson(scannedDetails, ExportedContactData::class.java)
            true
        } catch (_: Exception) {
            resetScannedContactDetails()
            false
        }
    }

    fun resetScannedContactDetails()
    {
        _scannedContactDetails.value = ExportedContactData("", "")
    }

    override fun reset()
    {
        super.reset()
        resetSearchQuery()
    }
}
