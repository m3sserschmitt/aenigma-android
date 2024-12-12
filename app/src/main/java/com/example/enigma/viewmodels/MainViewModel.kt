package com.example.enigma.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.enigma.crypto.CryptoProvider
import com.example.enigma.crypto.PublicKeyExtensions.getAddressFromPublicKey
import com.example.enigma.crypto.SignatureService
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.ContactWithConversationPreview
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.data.network.SignalRClient
import com.example.enigma.models.CreatedSharedData
import com.example.enigma.util.DatabaseRequestState
import com.example.enigma.models.ExportedContactData
import com.example.enigma.models.SharedData
import com.example.enigma.models.SharedDataCreate
import com.example.enigma.ui.navigation.Screens
import com.example.enigma.util.QrCodeGenerator
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val signatureService: SignatureService,
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

    private val _qrCode
            = MutableStateFlow<DatabaseRequestState<Bitmap>>(DatabaseRequestState.Idle)

    private val _qrCodeLabel = MutableStateFlow("")

    private val _scannedContactDetails = MutableStateFlow(ExportedContactData("", "", ""))

    private val _contactExportedData = MutableStateFlow(ExportedContactData("", "", ""))

    private val _sharedDataCreateResult
        = MutableStateFlow<DatabaseRequestState<CreatedSharedData>>(DatabaseRequestState.Idle)

    private val _sharedDataRequestResult
        = MutableStateFlow<DatabaseRequestState<SharedData>>(DatabaseRequestState.Idle)

    val allContacts: StateFlow<DatabaseRequestState<List<ContactWithConversationPreview>>> = _allContacts

    val qrCode: StateFlow<DatabaseRequestState<Bitmap>> = _qrCode

    val qrCodeLabel: StateFlow<String> = _qrCodeLabel

    val notificationsPermissionGranted: Flow<Boolean> = repository.local.notificationsAllowed

    val sharedDataCreateResult: StateFlow<DatabaseRequestState<CreatedSharedData>> = _sharedDataCreateResult

    val sharedDataRequest: StateFlow<DatabaseRequestState<SharedData>> = _sharedDataRequestResult

    val outgoingMessages: LiveData<List<MessageEntity>> get() = repository.local.getOutgoingMessages().asLiveData()

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

    fun generateCode(profileId: String)
    {
        if(_qrCode.value is DatabaseRequestState.Loading) return
        _qrCode.value = DatabaseRequestState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
               generateQrCodeBitmap(profileId).collect {
                   qrCode -> if(qrCode != null)
                       _qrCode.value = DatabaseRequestState.Success(qrCode)
                   else
                       _qrCode.value = DatabaseRequestState.Error(
                           Exception("Failed to generate contact QR Code")
                       )
               }
            } catch (ex: Exception)
            {
                _qrCode.value = DatabaseRequestState.Error(ex)
            }
        }
    }

    override fun getContactEntityForSaving(): ContactEntity? {
        val contactAddress = _scannedContactDetails.value.publicKey.getAddressFromPublicKey() ?: return null

        return ContactEntity(
            contactAddress,
            newContactName.value,
            _scannedContactDetails.value.publicKey,
            _scannedContactDetails.value.guardHostname,
            _scannedContactDetails.value.guardAddress,
            false,
            ZonedDateTime.now()
        )
    }

    override fun validateNewContactName(name: String): Boolean {
        return name.isNotBlank() && try {
            (_allContacts.value as DatabaseRequestState.Success).data.all { item ->
                item.name != name
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun getMyProfileBitmap(): Flow<Bitmap?>
    {
        return flow {
            val guard = repository.local.getGuard()

            if (guard != null && signatureService.address != null && signatureService.publicKey != null) {
                _contactExportedData.value = ExportedContactData(
                    guard.hostname,
                    guard.address,
                    signatureService.publicKey
                )

                emit(QrCodeGenerator(400, 400).encodeAsBitmap(_contactExportedData.value.toString()))
            } else {
                emit(null)
            }
        }
    }

    private fun getProfileBitmap(profileId: String): Flow<Bitmap?>
    {
        return flow {
            val contact = repository.local.getContact(profileId)

            if(contact != null)
            {
                _contactExportedData.value = ExportedContactData(
                    contact.guardHostname,
                    contact.guardAddress,
                    contact.publicKey
                )

                emit(QrCodeGenerator(400, 400).encodeAsBitmap(_contactExportedData.value.toString()))
            }
            else {
                emit(null)
            }
        }
    }

    private fun getQrCodeLabel(address: String): String
    {
        return if (address == Screens.ADD_CONTACT_SCREEN_SHARE_MY_CODE_ARG_VALUE)
        {
            "Sharing @My code"
        }
        else try {
            val contact = (allContacts.value as DatabaseRequestState.Success).data.find { item -> item.address == address }
            if(contact != null)
            {
                "Sharing @${contact.name}"
            }
            else
            {
                ""
            }
        }
        catch (_: Exception)
        {
            ""
        }
    }

    private fun generateQrCodeBitmap(profileId: String): Flow<Bitmap?>
    {
        _qrCodeLabel.value = getQrCodeLabel(profileId)
        return when(profileId) {
            Screens.ADD_CONTACT_SCREEN_SHARE_MY_CODE_ARG_VALUE -> {
                getMyProfileBitmap()
            }
            else -> {
                getProfileBitmap(profileId)
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
            val contactToUpdate = contact.toContact()
            contactToUpdate.name = newContactName.value
            repository.local.updateContact(contactToUpdate)
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

    fun createContactShareLink()
    {
        _sharedDataCreateResult.value = DatabaseRequestState.Loading
        viewModelScope.launch(defaultDispatcher) {
            try {
                val signature = signatureService.sign(_contactExportedData.value.toString().toByteArray())

                if (signature != null) {
                    val sharedDataCreate = SharedDataCreate(signature.first, signature.second)
                    val response = repository.remote.createSharedData(sharedDataCreate)
                    val body = response.body()

                    if (response.code() == 200 && body != null) {
                        _sharedDataCreateResult.value = DatabaseRequestState.Success(body)
                    }
                    else throw Exception()
                }
                else throw Exception()
            }
            catch (_: Exception) {
                _sharedDataCreateResult.value = DatabaseRequestState.Error(
                    Exception("Something went wrong while trying to create a link.")
                )
            }
        }
    }

    fun openContactSharedData(tag: String)
    {
        _sharedDataRequestResult.value = DatabaseRequestState.Loading
        viewModelScope.launch(defaultDispatcher) {
            try {
                val response = repository.remote.getSharedData(tag)
                val body = response.body()

                if(response.code() == 200 && body != null && body.data != null && body.publicKey != null)
                {
                    val content = CryptoProvider.getDataFromSignature(body.data, body.publicKey) ?: throw Exception()
                    val stringContent = String(content, Charsets.UTF_8)
                    _scannedContactDetails.value = Gson().fromJson(
                        stringContent,
                        ExportedContactData::class.java
                    )
                    _sharedDataRequestResult.value = DatabaseRequestState.Success(body)
                } else throw Exception()
            }
            catch (ex: Exception)
            {
                _sharedDataRequestResult.value = DatabaseRequestState.Error(
                    Exception("Could not process shared data. Invalid content or link.")
                )
            }
        }
    }

    private fun resetSearchQuery()
    {
        searchContacts("")
    }

    private fun resetSharedDataCreateResult()
    {
        _sharedDataCreateResult.value = DatabaseRequestState.Idle
    }

    private fun resetSharedDataRequestResult()
    {
        _sharedDataRequestResult.value = DatabaseRequestState.Idle
    }

    private fun resetScannedContactDetails()
    {
        _scannedContactDetails.value = ExportedContactData("", "", "")
    }

    override fun resetContactChanges()
    {
        resetNewContactName()
        resetScannedContactDetails()
        resetSharedDataRequestResult()
        resetSharedDataCreateResult()
    }

    override fun init()
    {
        resetNewContactName()
        resetSearchQuery()
    }
}
