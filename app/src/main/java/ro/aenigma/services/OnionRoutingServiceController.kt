package ro.aenigma.services

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.qualifiers.ApplicationContext
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
                    start()
                } else if (torStatus.shouldStopTor(torPreference)) {
                    delay(1000)
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

    fun stop() {
        val intent = Intent(appContext, OnionRoutingService::class.java)
        intent.action = TorService.ACTION_STOP
        appContext.stopService(intent)
    }
}
