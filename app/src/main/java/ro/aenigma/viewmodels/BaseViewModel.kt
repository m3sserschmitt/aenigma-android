/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

package ro.aenigma.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import ro.aenigma.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ro.aenigma.services.OkHttpClientProvider
import ro.aenigma.services.SignalrController
import ro.aenigma.workers.SignalRClientWorker
import ro.aenigma.workers.extensions.WorkManagerExtensions

abstract class BaseViewModel(
    protected val repository: Repository,
    protected val workManager: WorkManager,
    protected val signalrController: SignalrController,
    private val okHttpClientProviderLazy: dagger.Lazy<OkHttpClientProvider>,
): ViewModel() {

    private val _userName = MutableStateFlow("")

    private val _attachments = MutableStateFlow<List<String>>(listOf())

    private val _isForwardMode = MutableStateFlow(false)

    private val _isClientWorkerRunning = MutableStateFlow(false)

    fun provideOkHttpClientProvider(): OkHttpClientProvider {
        return okHttpClientProviderLazy.get()
    }

    val ioDispatcher = Dispatchers.IO

    val mainDispatcher = Dispatchers.Main

    val userName: StateFlow<String> = _userName

    val attachments: StateFlow<List<String>> = _attachments

    val isForwardMode: StateFlow<Boolean> = _isForwardMode

    val clientStatus = signalrController.clientStatus

    val isClientWorkerRunning: StateFlow<Boolean> = _isClientWorkerRunning

    abstract fun init()

    protected fun collectUserName() {
        viewModelScope.launch(ioDispatcher) {
            repository.local.name.catch { _userName.value = "" }
                .collect { userName -> _userName.value = userName }
        }
    }

    protected fun collectClientWork() {
        viewModelScope.launch(ioDispatcher) {
            combine(
                workManager.getWorkInfosForUniqueWorkFlow(WorkManagerExtensions.SYNC_AND_INVOKE_CLIENT_UNIQUE_WORK_NAME),
                workManager.getWorkInfosForUniqueWorkFlow(SignalRClientWorker.UNIQUE_ONE_TIME_REQUEST)
            ) { syncWork, clientWork -> syncWork to clientWork }
                .collect { (syncWork, clientWork) ->
                    val syncWorkStatesSet =
                        syncWork.mapTo(mutableSetOf()) { workInfo -> workInfo.state }
                    val clientWorkStatesSet =
                        clientWork.mapTo(mutableSetOf()) { workInfo -> workInfo.state }
                    val unionWorkStatesSet = syncWorkStatesSet.union(clientWorkStatesSet)
                    _isClientWorkerRunning.value = setOf(
                        WorkInfo.State.ENQUEUED,
                        WorkInfo.State.RUNNING,
                        WorkInfo.State.BLOCKED
                    ).intersect(unionWorkStatesSet).isNotEmpty()
                }
        }
    }

    fun syncAndReconnect() {
        signalrController.enqueueSyncGraphAndReconnect()
    }

    fun setAttachments(attachments: List<String>) {
        _attachments.value = attachments
        _isForwardMode.value = attachments.isNotEmpty()
    }
}
