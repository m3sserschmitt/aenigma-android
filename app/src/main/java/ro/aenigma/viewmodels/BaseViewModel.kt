package ro.aenigma.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ro.aenigma.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ro.aenigma.services.OkHttpClientProvider
import ro.aenigma.services.SignalrController

abstract class BaseViewModel(
    protected val repository: Repository,
    private val signalrController: SignalrController,
    private val okHttpClientProviderLazy: dagger.Lazy<OkHttpClientProvider>
): ViewModel() {

    protected var ioDispatcher = Dispatchers.IO

    private val _userName = MutableStateFlow("")

    private val _attachments = MutableStateFlow<List<String>>(listOf())

    private val _isForwardMode = MutableStateFlow(false)

    init {
        collectUserName()
    }

    fun provideOkHttpClientProvider(): OkHttpClientProvider {
        return okHttpClientProviderLazy.get()
    }

    protected var defaultDispatcher = Dispatchers.Default

    val userName: StateFlow<String> = _userName

    val attachments: StateFlow<List<String>> = _attachments

    val isForwardMode: StateFlow<Boolean> = _isForwardMode

    val clientStatus = signalrController.clientStatus

    abstract fun init()

    fun collectUserName() {
        viewModelScope.launch(ioDispatcher) {
            repository.local.name.catch { _userName.value = "" }
                .collect { userName -> _userName.value = userName }
        }
    }

    fun retryClientConnection() {
        signalrController.resetClient()
    }

    fun setAttachments(attachments: List<String>) {
        _attachments.value = attachments
        _isForwardMode.value = attachments.isNotEmpty()
    }
}
