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
