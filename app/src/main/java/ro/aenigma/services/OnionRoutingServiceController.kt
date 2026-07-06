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
import info.guardianproject.netcipher.proxy.OrbotHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.torproject.jni.TorService
import ro.aenigma.data.Repository
import ro.aenigma.models.extensions.TorStatusExtensions.shouldStartTor
import ro.aenigma.models.extensions.TorStatusExtensions.shouldStopTor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class OnionRoutingServiceController @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val onionRoutingServiceMonitor: OnionRoutingServiceMonitor,
    private val repository: Repository
) {
    fun observeTorPreferences(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            combine(
                repository.local.useTor,
                repository.local.useOrbot,
                onionRoutingServiceMonitor.torStatus
            ) { torPreference, orbotPreference, torStatus ->
                Triple(torPreference, orbotPreference, torStatus)
            }.distinctUntilChanged().collect { (torPreference, orbotPreference, torStatus) ->
                if (torStatus.shouldStartTor(torPreference) && !orbotPreference) {
                    delay(2.seconds)
                    start()
                } else if (torStatus.shouldStartTor(orbotPreference) && !torPreference) {
                    delay(2.seconds)
                    startOrbot()
                } else if (torStatus.shouldStopTor(torPreference)) {
                    stop()
                }
            }
        }
    }

    fun start() {
        val intent = Intent(appContext, OnionRoutingService::class.java)
        intent.action = TorService.ACTION_START
        appContext.startService(intent)
    }

    fun startOrbot() {
        OrbotHelper.requestStartTor(appContext)
    }

    fun stop() {
        val intent = Intent(appContext, OnionRoutingService::class.java)
        intent.action = TorService.ACTION_STOP
        appContext.stopService(intent)
    }
}
