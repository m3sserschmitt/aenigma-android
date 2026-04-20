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

    init {
        viewModelScope.launch(ioDispatcher) {
            repository.local.name.catch { _userName.value = "" }
                .collect { userName -> _userName.value = userName }
        }
    }

    fun provideOkHttpClientProvider(): OkHttpClientProvider {
        return okHttpClientProviderLazy.get()
    }

    protected var defaultDispatcher = Dispatchers.Default

    val userName: StateFlow<String> = _userName

    val clientStatus = signalrController.clientStatus

    abstract fun init()

    fun retryClientConnection() {
        signalrController.resetClient()
    }
}
