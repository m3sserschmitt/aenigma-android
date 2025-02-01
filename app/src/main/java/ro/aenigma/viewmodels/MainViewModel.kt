package ro.aenigma.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ro.aenigma.R
import ro.aenigma.AenigmaApp
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.ContactWithConversationPreview
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.network.EnigmaApi
import ro.aenigma.data.network.SignalRClient
import ro.aenigma.models.CreatedSharedData
import ro.aenigma.util.RequestState
import ro.aenigma.models.ExportedContactData
import ro.aenigma.models.SharedData
import ro.aenigma.models.SharedDataCreate
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
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
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
        MutableStateFlow<RequestState<List<ContactWithConversationPreview>>>(
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

    val allContacts: StateFlow<RequestState<List<ContactWithConversationPreview>>> =
        _allContacts

    val qrCode: StateFlow<RequestState<Bitmap>> = _qrCode

    val qrCodeLabel: StateFlow<String> = _qrCodeLabel

    val notificationsPermissionGranted: Flow<Boolean> = repository.local.notificationsAllowed

    val sharedDataCreateResult: StateFlow<RequestState<CreatedSharedData>> =
        _sharedDataCreateResult

    val sharedDataRequest: StateFlow<RequestState<SharedData>> = _sharedDataRequestResult

    val outgoingMessages: LiveData<List<MessageEntity>>
        get() = repository.local.getOutgoingMessages().asLiveData()

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
                repository.local.getContactsWithConversationPreviewFlow().collect { contacts ->
                    val query = _contactsSearchQuery.value
                    val result = if (query.isNotBlank())
                        contacts.filter { contact -> contact.name.contains(query) }
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
                        repository.local.getContactsWithConversationPreview()
                    else
                        repository.local.searchContacts(query).map { item ->
                            item.toContactWithPreview()
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
            (_allContacts.value as RequestState.Success).data.all { item ->
                item.name != name
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun getMyProfileBitmap(): Flow<Bitmap?> {
        return flow {
            val guard = repository.local.getGuard()

            if (guard != null && signatureService.address != null && signatureService.publicKey != null) {
                _contactExportedData.value = ExportedContactData(
                    guard.hostname,
                    guard.address,
                    signatureService.publicKey
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
                (allContacts.value as RequestState.Success).data.find { item -> item.address == address }
            contact?.name ?: ""
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

    fun deleteContacts(contacts: List<ContactWithConversationPreview>) {
        viewModelScope.launch(ioDispatcher) {
            repository.local.removeContacts(contacts.map { contact -> contact.toContact() })
        }
    }

    fun renameContact(contact: ContactWithConversationPreview) {
        viewModelScope.launch(ioDispatcher) {
            val contactToUpdate = contact.toContact()
            contactToUpdate.name = newContactName.value
            repository.local.updateContact(contactToUpdate)
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
            try {
                val signature =
                    signatureService.sign(_contactExportedData.value.toString().toByteArray())

                if (signature != null) {
                    val sharedDataCreate = SharedDataCreate(signature.first, signature.second, 3)
                    val response = repository.remote.createSharedData(sharedDataCreate)
                    val body = response.body()

                    if (response.code() == 200 && body != null) {
                        _sharedDataCreateResult.value = RequestState.Success(body)
                    } else throw Exception()
                } else throw Exception()
            } catch (_: Exception) {
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
                val uri = Uri.parse(url)
                val tag = uri.getQueryParameter("Tag") ?: uri.getQueryParameter("tag")
                ?: throw Exception()
                val response = initApi(url)?.getSharedData(tag) ?: throw Exception()
                val body = response.body() ?: throw Exception()

                if (response.code() == 200 && body.data != null && body.publicKey != null) {
                    val content = CryptoProvider.getDataFromSignature(body.data, body.publicKey)
                        ?: throw Exception()
                    val stringContent = String(content, Charsets.UTF_8)
                    _scannedContactDetails.value = Gson().fromJson(
                        stringContent,
                        ExportedContactData::class.java
                    )
                    _sharedDataRequestResult.value = RequestState.Success(body)
                } else throw Exception()
            } catch (ex: Exception) {
                _sharedDataRequestResult.value = RequestState.Error(
                    Exception("Could not process shared data. Invalid content or link.")
                )
            }
        }
    }

    private fun initApi(url: String): EnigmaApi? {
        return try {
            val parsedUrl = URL(url)
            Retrofit.Builder()
                .baseUrl("${parsedUrl.protocol}://${parsedUrl.host}")
                .client(
                    OkHttpClient.Builder()
                        .readTimeout(10, TimeUnit.SECONDS)
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build().create(EnigmaApi::class.java)
        } catch (_: Exception) {
            null
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
