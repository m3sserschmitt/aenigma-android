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

package ro.aenigma.services

import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint
import org.torproject.jni.TorService
import ro.aenigma.R
import javax.inject.Inject

@AndroidEntryPoint
class OnionRoutingService @Inject constructor(): TorService() {
    @Inject
    lateinit var notifier: Notifier

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification =
            notifier.createTorServiceNotification(getString(R.string.tor_starting))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                Notifier.TOR_NOTIFICATION_ID,
                notification,
                FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
            )
        } else {
            startForeground(Notifier.TOR_NOTIFICATION_ID, notification)
        }
        return super.onStartCommand(intent, flags, startId)
    }
}
