package ro.aenigma.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import ro.aenigma.R
import ro.aenigma.AenigmaApp
import ro.aenigma.crypto.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.network.EnigmaApi
import ro.aenigma.data.network.SignalRClient
import ro.aenigma.models.CreatedSharedData
import ro.aenigma.util.RequestState
import ro.aenigma.models.ExportedContactData
import ro.aenigma.models.SharedData
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.util.QrCodeGenerator
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ro.aenigma.crypto.getStringDataFromSignature
import ro.aenigma.data.RemoteDataSource
import ro.aenigma.data.database.ContactWithLastMessage
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageActionType
import ro.aenigma.util.getQueryParameter
import ro.aenigma.workers.GroupUploadWorker
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
    init {
        viewModelScope.launch(ioDispatcher) {
            repository.local.notificationsAllowed.collect { allowed ->
                _notificationsAllowed.value = allowed
            }
        }
    }

    @Inject
    lateinit var workManager: dagger.Lazy<WorkManager>

    private val _contactsSearchQuery: MutableStateFlow<String> = MutableStateFlow("")

    private val _allContacts =
        MutableStateFlow<RequestState<List<ContactWithLastMessage>>>(
            RequestState.Idle
        )

    private val _qrCode = MutableStateFlow<RequestState<Bitmap>>(RequestState.Idle)

    private val _qrCodeLabel = MutableStateFlow("")

    private val _scannedContactDetails = MutableStateFlow(ExportedContactData("", "", ""))

    private val _contactExportedData = MutableStateFlow(ExportedContactData("", "", ""))

    private val _sharedDataCreateResult =
        MutableStateFlow<RequestState<CreatedSharedData>>(RequestState.Idle)

    private val _sharedDataRequestResult =
        MutableStateFlow<RequestState<SharedData>>(RequestState.Idle)

    private val _notificationsAllowed = MutableStateFlow(true)

    val allContacts: StateFlow<RequestState<List<ContactWithLastMessage>>> = _allContacts

    val qrCode: StateFlow<RequestState<Bitmap>> = _qrCode

    val qrCodeLabel: StateFlow<String> = _qrCodeLabel

    val sharedDataCreateResult: StateFlow<RequestState<CreatedSharedData>> =
        _sharedDataCreateResult

    val sharedDataRequest: StateFlow<RequestState<SharedData>> = _sharedDataRequestResult

    val notificationsAllowed: StateFlow<Boolean> = _notificationsAllowed

    fun loadContacts() {
        if (_allContacts.value is RequestState.Success
            || _allContacts.value is RequestState.Loading
        ) return

        _allContacts.value = RequestState.Loading
        collectContacts()
        collectSearches()
    }

    private fun collectContacts() {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.local.getContactWithLastMessageFlow().collect { contacts ->
                    val query = _contactsSearchQuery.value
                    val result = if (query.isNotBlank())
                        contacts.filter { contact -> contact.contact.name.contains(query) }
                    else
                        contacts

                    _allContacts.value = RequestState.Success(result)
                }
            } catch (ex: Exception) {
                _allContacts.value = RequestState.Error(ex)
            }
        }
    }

    private fun collectSearches() {
        viewModelScope.launch(defaultDispatcher) {
            _contactsSearchQuery.collect { query ->
                _allContacts.value = RequestState.Loading
                try {
                    val searchResult = if (query.isBlank())
                        repository.local.getContactWithLastMessage()
                    else
                        repository.local.searchContacts(query).map { item ->
                            ContactWithLastMessage(item, null)
                        }
                    _allContacts.value = RequestState.Success(searchResult)
                } catch (ex: Exception) {
                    _allContacts.value = RequestState.Error(ex)
                }
            }
        }
    }

    fun searchContacts(searchQuery: String) {
        _contactsSearchQuery.update { searchQuery }
    }

    fun generateCode(profileId: String) {
        if (_qrCode.value is RequestState.Loading) return
        _qrCode.value = RequestState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                generateQrCodeBitmap(profileId).collect { qrCode ->
                    if (qrCode != null)
                        _qrCode.value = RequestState.Success(qrCode)
                    else
                        _qrCode.value = RequestState.Error(
                            Exception("Failed to generate contact QR Code")
                        )
                }
            } catch (ex: Exception) {
                _qrCode.value = RequestState.Error(ex)
            }
        }
    }

    override fun getContactEntityForSaving(): ContactEntity? {
        val contactAddress =
            _scannedContactDetails.value.publicKey.getAddressFromPublicKey() ?: return null

        return ContactEntity(
            address = contactAddress,
            name = newContactName.value,
            publicKey = _scannedContactDetails.value.publicKey,
            guardHostname = _scannedContactDetails.value.guardHostname,
            guardAddress = _scannedContactDetails.value.guardAddress,
            type = ContactType.CONTACT,
            hasNewMessage = false,
            lastSynchronized = ZonedDateTime.now()
        )
    }

    override fun validateNewContactName(name: String): Boolean {
        return name.isNotBlank() && try {
            (_allContacts.value as RequestState.Success).data.all { item ->
                item.contact.name != name
            }
        } catch (_: Exception) {
            false
        }
    }

    fun createGroup(contacts: List<ContactWithLastMessage>) {
        viewModelScope.launch(ioDispatcher) {
            val memberAddresses = contacts.map { item -> item.contact.address }
            GroupUploadWorker.createOrUpdateGroupWorkRequest(
                workManager = workManager.get(),
                groupName = newContactName.value,
                userName = userName.value,
                members = memberAddresses,
                existingGroupAddress = null,
                actionType = MessageActionType.GROUP_CREATE
            )
        }
    }

    fun setupName(name: String) {
        viewModelScope.launch(ioDispatcher) {
            repository.local.saveName(name)
        }
    }

    private fun getMyProfileBitmap(): Flow<Bitmap?> {
        return flow {
            val guard = repository.local.getGuard()

            if (guard != null && signatureService.address != null && signatureService.publicKey != null) {
                _contactExportedData.value = ExportedContactData(
                    guard.hostname,
                    guard.address,
                    signatureService.publicKey!!
                )

                emit(
                    QrCodeGenerator(400, 400).encodeAsBitmap(_contactExportedData.value.toString())
                )
            } else {
                emit(null)
            }
        }
    }

    private fun getProfileBitmap(profileId: String): Flow<Bitmap?> {
        return flow {
            val contact = repository.local.getContact(profileId)

            if (contact != null) {
                _contactExportedData.value = ExportedContactData(
                    contact.guardHostname,
                    contact.guardAddress,
                    contact.publicKey
                )

                emit(
                    QrCodeGenerator(400, 400).encodeAsBitmap(_contactExportedData.value.toString())
                )
            } else {
                emit(null)
            }
        }
    }

    private fun getQrCodeLabel(address: String): String {
        return if (address == Screens.ADD_CONTACT_SCREEN_SHARE_MY_CODE_ARG_VALUE) {
            getApplication<AenigmaApp>().getString(R.string.my_code)
        } else try {
            val contact =
                (allContacts.value as RequestState.Success).data.find { item -> item.contact.address == address }
            contact?.contact?.name ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun generateQrCodeBitmap(profileId: String): Flow<Bitmap?> {
        _qrCodeLabel.value = getQrCodeLabel(profileId)
        return when (profileId) {
            Screens.ADD_CONTACT_SCREEN_SHARE_MY_CODE_ARG_VALUE -> {
                getMyProfileBitmap()
            }

            else -> {
                getProfileBitmap(profileId)
            }
        }
    }

    fun deleteContacts(contacts: List<ContactWithLastMessage>) {
        viewModelScope.launch(ioDispatcher) {
            repository.local.removeContacts(contacts.map { contact -> contact.contact })
        }
    }

    fun renameContact(contact: ContactWithLastMessage) {
        viewModelScope.launch(ioDispatcher) {
            contact.contact.name = newContactName.value
            repository.local.updateContact(contact.contact)
        }
    }

    fun saveNotificationsPreference(granted: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            repository.local.saveNotificationsAllowed(granted)
        }
    }

    fun setScannedContactDetails(scannedDetails: String): Boolean {
        return try {
            _scannedContactDetails.value =
                Gson().fromJson(scannedDetails, ExportedContactData::class.java)
            true
        } catch (_: Exception) {
            resetScannedContactDetails()
            false
        }
    }

    fun createContactShareLink() {
        _sharedDataCreateResult.value = RequestState.Loading
        viewModelScope.launch(defaultDispatcher) {
            val response = repository.remote.createSharedData(_contactExportedData.value.toString())
            if (response != null) {
                _sharedDataCreateResult.value = RequestState.Success(response)
            } else {

                _sharedDataCreateResult.value = RequestState.Error(
                    Exception("Something went wrong while trying to create a link.")
                )
            }
        }
    }

    fun openContactSharedData(url: String) {
        _sharedDataRequestResult.value = RequestState.Loading
        viewModelScope.launch(defaultDispatcher) {
            try {
                val tag = url.getQueryParameter("tag") ?: throw Exception()
                val response =
                    RemoteDataSource(EnigmaApi.initApi(url), signatureService).getSharedData(tag)
                        ?: throw Exception()
                val content = response.data.getStringDataFromSignature(response.publicKey!!)
                    ?: throw Exception()
                _scannedContactDetails.value = Gson().fromJson(
                    content,
                    ExportedContactData::class.java
                )
                _sharedDataRequestResult.value = RequestState.Success(response)
            } catch (ex: Exception) {
                _sharedDataRequestResult.value = RequestState.Error(
                    Exception("Could not process shared data. Invalid content or link.")
                )
            }
        }
    }

    private fun resetSearchQuery() {
        searchContacts("")
    }

    private fun resetSharedDataCreateResult() {
        _sharedDataCreateResult.value = RequestState.Idle
    }

    private fun resetSharedDataRequestResult() {
        _sharedDataRequestResult.value = RequestState.Idle
    }

    private fun resetScannedContactDetails() {
        _scannedContactDetails.value = ExportedContactData("", "", "")
    }

    override fun resetContactChanges() {
        resetNewContactName()
        resetScannedContactDetails()
        resetSharedDataRequestResult()
        resetSharedDataCreateResult()
    }

    override fun init() {
        resetNewContactName()
        resetSearchQuery()
    }
}
