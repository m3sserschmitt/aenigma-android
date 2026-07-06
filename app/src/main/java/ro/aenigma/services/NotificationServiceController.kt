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

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ro.aenigma.data.Repository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationServiceController @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val repository: Repository
) {
    fun observeNotificationServicePreference(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            repository.local.notificationServicePreference.collect { notificationServiceActive ->
                if (notificationServiceActive) {
                    start()
                } else {
                    stop()
                }
            }
        }
    }

    fun start() {
        val intent = Intent(appContext, NotificationService::class.java)
        intent.action = NotificationService.ACTION_START
        appContext.startService(intent)
    }

    fun stop() {
        val intent = Intent(appContext, NotificationService::class.java)
        intent.action = NotificationService.ACTION_STOP
        appContext.stopService(intent)
    }
}
