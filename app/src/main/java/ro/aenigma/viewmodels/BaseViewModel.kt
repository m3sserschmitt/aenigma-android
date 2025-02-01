package ro.aenigma.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.network.SignalRClient
import ro.aenigma.data.network.SignalRStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

abstract class BaseViewModel(
    protected val repository: Repository,
    private val signalRClient: SignalRClient,
    application: Application,
): AndroidViewModel(application) {

    private val _newContactName: MutableStateFlow<String> = MutableStateFlow("")

    protected var ioDispatcher = Dispatchers.IO

    protected var defaultDispatcher = Dispatchers.Default

    val signalRClientStatus: LiveData<SignalRStatus> = signalRClient.status

    val newContactName: StateFlow<String> = _newContactName

    protected abstract fun validateNewContactName(name: String): Boolean

    protected abstract fun getContactEntityForSaving(): ContactEntity?

    protected abstract fun resetContactChanges()

    abstract fun init()

    protected fun resetNewContactName()
    {
        _newContactName.value = ""
    }

    fun saveContactChanges()
    {
        val contact = getContactEntityForSaving() ?: return
        viewModelScope.launch(ioDispatcher) {
            repository.local.insertOrUpdateContact(contact)
        }
        resetContactChanges()
    }

    fun setNewContactName(newValue: String): Boolean
    {
        _newContactName.value = newValue
        return try {
            validateNewContactName(newValue)
        } catch (_: Exception) {
            false
        }
    }

    fun retryClientConnection()
    {
        signalRClient.resetAborted()
    }

    fun cleanupContactChanges()
    {
        resetContactChanges()
    }
}
