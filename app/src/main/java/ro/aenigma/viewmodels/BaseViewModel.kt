package ro.aenigma.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ro.aenigma.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ro.aenigma.services.SignalrConnectionController

abstract class BaseViewModel(
    protected val repository: Repository,
    private val signalrConnectionController: SignalrConnectionController,
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

    val userName: StateFlow<String> = _userName

    val clientStatus = signalrConnectionController.clientStatus

    abstract fun init()

    fun retryClientConnection() {
        signalrConnectionController.resetClient()
    }
}
