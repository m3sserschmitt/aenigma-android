package ro.aenigma.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import ro.aenigma.crypto.extensions.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.models.CreatedSharedData
import ro.aenigma.util.RequestState
import ro.aenigma.models.ExportedContactData
import ro.aenigma.models.SharedData
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.util.QrCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ro.aenigma.crypto.extensions.SignatureExtensions.getStringDataFromSignature
import ro.aenigma.data.database.ContactWithLastMessage
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withName
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.models.QrCodeDto
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.services.SignalrConnectionController
import ro.aenigma.util.SerializerExtensions.fromJson
import ro.aenigma.util.SerializerExtensions.toJson
import ro.aenigma.workers.GroupUploadWorker
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val signatureService: SignatureService,
    repository: Repository,
    application: Application,
    signalrConnectionController: SignalrConnectionController,
) : BaseViewModel(
    repository,
    signalrConnectionController,
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

    private val _qrCode = MutableStateFlow<RequestState<QrCodeDto>>(RequestState.Idle)

    private val _importedContactDetails = MutableStateFlow<ExportedContactData?>(null)

    private val _exportedContactDetails = MutableStateFlow<ExportedContactData?>(null)

    private val _sharedDataCreateResult =
        MutableStateFlow<RequestState<CreatedSharedData>>(RequestState.Idle)

    private val _sharedDataRequestResult =
        MutableStateFlow<RequestState<SharedData>>(RequestState.Idle)

    private val _notificationsAllowed = MutableStateFlow(true)

    private val _useTor = MutableStateFlow(false)

    val allContacts: StateFlow<RequestState<List<ContactWithLastMessage>>> = _allContacts

    val qrCode: StateFlow<RequestState<QrCodeDto>> = _qrCode

    val sharedDataCreateResult: StateFlow<RequestState<CreatedSharedData>> = _sharedDataCreateResult

    val sharedDataRequest: StateFlow<RequestState<SharedData>> = _sharedDataRequestResult

    val importedContactDetails: StateFlow<ExportedContactData?> = _importedContactDetails

    val notificationsAllowed: StateFlow<Boolean> = _notificationsAllowed

    val useTor: StateFlow<Boolean> = _useTor

    init {
        loadContacts()
        collectUseTor()
    }

    fun loadContacts() {
        if (_allContacts.value is RequestState.Success
            || _allContacts.value is RequestState.Loading
        ) return

        _allContacts.value = RequestState.Loading
        collectContacts()
        collectSearches()
    }

    private fun collectUseTor() {
        viewModelScope.launch(ioDispatcher) {
            repository.local.useTor.collect {
                useTor -> _useTor.value = useTor
            }
        }
    }

    private fun collectContacts() {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.local.getContactWithMessagesFlow().collect { contacts ->
                    val query = _contactsSearchQuery.value
                    val result = if (query.isNotBlank())
                        contacts.filter { contact ->
                            contact.contact.name?.contains(
                                query,
                                ignoreCase = true
                            ) == true
                        }
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
                        repository.local.getContactWithMessages()
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

    fun saveNewContact(name: String) {
        val contactAddress =
            _importedContactDetails.value?.publicKey.getAddressFromPublicKey() ?: return
        val publicKey = _importedContactDetails.value?.publicKey ?: return
        val guardAddress = _importedContactDetails.value?.guardAddress ?: return
        val newContact = ContactEntityFactory.createContact(
            address = contactAddress,
            name = name,
            publicKey = publicKey,
            guardHostname = _importedContactDetails.value?.guardHostname,
            guardAddress = guardAddress,
        )
        viewModelScope.launch(ioDispatcher) {
            repository.local.insertOrUpdateContact(newContact)
        }
        resetContactChanges()
    }

    fun validateNewContactName(name: String): Boolean {
        return name.isNotBlank() && try {
            (_allContacts.value as RequestState.Success).data.all { item ->
                item.contact.name != name
            }
        } catch (_: Exception) {
            false
        }
    }

    fun createGroup(contacts: List<ContactWithLastMessage>, name: String) {
        viewModelScope.launch(ioDispatcher) {
            val memberAddresses = contacts.map { item -> item.contact.address }
            GroupUploadWorker.createOrUpdateGroupWorkRequest(
                workManager = workManager.get(),
                groupName = name,
                userName = userName.value,
                members = memberAddresses,
                existingGroupAddress = null,
                actionType = MessageType.GROUP_CREATE
            )
        }
    }

    fun setupName(name: String) {
        viewModelScope.launch(ioDispatcher) {
            repository.local.saveName(name)
        }
    }

    fun resetUserName() {
        viewModelScope.launch(ioDispatcher) {
            repository.local.saveName("")
        }
    }

    fun useTorChanged(useTor: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            repository.local.saveTorPreference(useTor)
        }
    }

    private fun getMyProfileBitmap(): Flow<QrCodeDto?> {
        return flow {
            val guard = repository.local.getGuard()

            if (guard != null && signatureService.address != null && signatureService.publicKey != null) {
                _exportedContactDetails.value = ExportedContactData(
                    guardHostname = guard.hostname,
                    guardAddress = guard.address,
                    publicKey = signatureService.publicKey!!,
                    userName = userName.value
                )
                val code = QrCodeGenerator(400, 400)
                    .encodeAsBitmap(_exportedContactDetails.value.toJson())
                if (code != null) {
                    emit(
                        QrCodeDto(
                            code, "@${userName.value}", true
                        )
                    )
                } else {
                    emit(null)
                }
            } else {
                emit(null)
            }
        }
    }

    private fun getProfileBitmap(profileId: String): Flow<QrCodeDto?> {
        return flow {
            val contact = repository.local.getContact(profileId)

            if (contact != null) {
                _exportedContactDetails.value = ExportedContactData(
                    contact.guardHostname,
                    contact.guardAddress,
                    contact.publicKey,
                    contact.name
                )
                val code = QrCodeGenerator(400, 400)
                    .encodeAsBitmap(_exportedContactDetails.value.toJson())
                if(code != null) {
                    emit(QrCodeDto(code, "@${contact.name.toString()}", false))
                } else {
                    emit(null)
                }
            } else {
                emit(null)
            }
        }
    }

    private fun generateQrCodeBitmap(profileId: String): Flow<QrCodeDto?> {
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

    fun renameContact(contact: ContactWithLastMessage, name: String) {
        when (contact.contact.type) {
            ContactType.CONTACT -> viewModelScope.launch(ioDispatcher) {
                val updatedContact = contact.contact.withName(name)
                updatedContact?.let { repository.local.updateContact(it) }
            }

            ContactType.GROUP -> GroupUploadWorker.createOrUpdateGroupWorkRequest(
                workManager = workManager.get(),
                userName = userName.value,
                groupName = name,
                existingGroupAddress = contact.contact.address,
                actionType = MessageType.GROUP_RENAMED,
                members = null
            )
        }
    }

    fun saveNotificationsPreference(granted: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            repository.local.saveNotificationsAllowed(granted)
        }
    }

    fun setScannedContactDetails(scannedDetails: ExportedContactData) {
        _importedContactDetails.value = scannedDetails
    }

    fun createContactShareLink() {
        _sharedDataCreateResult.value = RequestState.Loading
        viewModelScope.launch(defaultDispatcher) {
            val data = _exportedContactDetails.value.toJson()
            if (data != null) {
                val response = repository.remote.createSharedData(data)
                if (response != null) {
                    _sharedDataCreateResult.value = RequestState.Success(response)
                } else {
                    _sharedDataCreateResult.value = RequestState.Error(
                        Exception("Something went wrong while trying to create a link.")
                    )
                }
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
                val response = repository.remote.getSharedDataByUrl(url) ?: throw Exception()
                val content = response.data.getStringDataFromSignature(response.publicKey!!)
                    ?: throw Exception()
                _importedContactDetails.value =
                    content.fromJson<ExportedContactData>() ?: throw Exception()
                _sharedDataRequestResult.value = RequestState.Success(response)
            } catch (_: Exception) {
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
        _importedContactDetails.value = null
    }

    fun resetContactChanges() {
        resetScannedContactDetails()
        resetSharedDataRequestResult()
        resetSharedDataCreateResult()
    }

    override fun init() {
        resetSearchQuery()
        resetContactChanges()
    }
}
