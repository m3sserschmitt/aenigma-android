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

package ro.aenigma.workers

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import ro.aenigma.data.Repository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ro.aenigma.R
import ro.aenigma.services.ClientAction
import ro.aenigma.services.Notifier
import ro.aenigma.services.SignalrController
import java.util.concurrent.TimeUnit

@HiltWorker
class SignalRClientWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val signalrController: SignalrController,
    private val repository: Repository,
    private val notifier: Notifier
) : CoroutineWorker(context, params) {

    companion object {
        const val ACTION_ARG = "action"
        const val UNIQUE_ONE_TIME_REQUEST = "signalr-client-worker"
        const val UNIQUE_PERIODIC_WORK_REQUEST = "SIGNALR_PERIODIC_CONNECTION"
        const val PERIODIC_WORK_REPEAT_INTERVAL: Long = 15
        val PERIODIC_WORK_REQUEST_TIME_UNIT = TimeUnit.MINUTES
        private const val MAX_RETRY_COUNT = 3
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRY_COUNT) {
            return Result.failure()
        }
        val action = ClientAction(inputData.getInt(ACTION_ARG, 0))
        var ok = true
        if (signalrController.isConnected() && action contains ClientAction.Disconnect) {
            ok = signalrController.disconnect()
        }

        if (ok && !signalrController.isConnected() && action contains ClientAction.Connect) {
            ok = signalrController.connect(repository.local.getGuardHostname())
        }

        if (ok && signalrController.isAuthenticated() && action contains ClientAction.Pull) {
            ok = signalrController.pull()
        }

        if (ok && signalrController.isAuthenticated() && action contains ClientAction.Cleanup) {
            ok = signalrController.cleanup()
        }

        return if (ok) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ForegroundInfo(
            id.hashCode(),
            notifier.createWorkerNotification(applicationContext.getString(R.string.connecting_server)),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        ) else
            ForegroundInfo(
                id.hashCode(),
                notifier.createWorkerNotification(applicationContext.getString(R.string.connecting_server))
            )
    }
}
