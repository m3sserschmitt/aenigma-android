package ro.aenigma.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import ro.aenigma.data.Repository
import ro.aenigma.data.network.SignalRClient
import ro.aenigma.data.network.SignalRStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel(
    protected val repository: Repository,
    private val signalRClient: SignalRClient,
    application: Application,
): AndroidViewModel(application) {

    protected var ioDispatcher = Dispatchers.IO

    private val _userName = MutableStateFlow("")

    init {
        viewModelScope.launch(ioDispatcher) {
            repository.local.name.collect { userName -> _userName.value = userName }
        }
    }

    protected var defaultDispatcher = Dispatchers.Default

    val signalRClientStatus: LiveData<SignalRStatus> = signalRClient.status

    val userName: StateFlow<String> = _userName

    abstract fun init()

    fun retryClientConnection()
    {
        signalRClient.resetAborted()
    }
}
