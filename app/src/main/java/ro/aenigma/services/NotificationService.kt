package ro.aenigma.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import ro.aenigma.R
import ro.aenigma.activities.AppActivity
import ro.aenigma.models.ContactDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.enums.MessageType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        const val TOR_NOTIFICATION_ID = 1371
        private const val TOR_SERVICE_CHANNEL_ID = "tor-service-channel"
        private const val TOR_SERVICE_CHANNEL_NAME = "Tor Service Notification"
        private const val TOR_SERVICE_CHANNEL_DESCRIPTION =
            "Channel used for Tor Foreground Service"
        private const val WORKERS_CHANNEL_ID = "workers-service-channel"
        private const val WORKERS_CHANNEL_NAME = "Background Worker Notification"
        private const val WORKERS_CHANNEL_DESCRIPTION = "Channel used for Background workers"
        private const val NEW_MESSAGE_CHANNEL_ID = "new-message-channel"
        private const val NEW_MESSAGE_CHANNEL_NAME = "New message notifications"
        private const val NEW_MESSAGE_CHANNEL_DESCRIPTION =
            "Channel used to display notification related to incoming messages"
    }

    private fun createNotificationChannel(
        channel: String,
        name: String,
        description: String
    ) {
        val notificationChannel =
            NotificationChannel(channel, name, NotificationManager.IMPORTANCE_HIGH).apply {
                this.description = description
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun createChatNavigationIntent(): PendingIntent {
        val intent = Intent(context, AppActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun notify(notification: Notification, id: Int) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(id, notification)
            }
        }
    }

    private val _currentChat = MutableStateFlow<String?>(null)

    private val _isBackground = MutableStateFlow(true)

    private val _allChatsBlocked = MutableStateFlow(false)

    fun createTorServiceNotification(text: String): Notification {
        createNotificationChannel(
            TOR_SERVICE_CHANNEL_ID, TOR_SERVICE_CHANNEL_NAME, TOR_SERVICE_CHANNEL_DESCRIPTION
        )
        return NotificationCompat.Builder(context, TOR_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vpn)
            .setContentTitle(context.getString(R.string.tor_service))
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            ).setSilent(true)
            .build()
    }

    fun createWorkerNotification(text: String): Notification {
        createNotificationChannel(
            WORKERS_CHANNEL_ID, WORKERS_CHANNEL_NAME, WORKERS_CHANNEL_DESCRIPTION
        )
        return NotificationCompat.Builder(context, WORKERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_api)
            .setContentTitle(context.getString(R.string.background_work))
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            ).setSilent(true)
            .build()
    }

    fun notifyTorStatus(text: String) {
        notify(createTorServiceNotification(text), TOR_NOTIFICATION_ID)
    }

    fun notifyNewMessage(contact: ContactDto, messageEntity: MessageDto) {
        if (!_isBackground.value && (_currentChat.value == contact.address || _allChatsBlocked.value)) {
            return
        }

        createNotificationChannel(
            NEW_MESSAGE_CHANNEL_ID,
            NEW_MESSAGE_CHANNEL_NAME,
            NEW_MESSAGE_CHANNEL_DESCRIPTION
        )
        val intent = createChatNavigationIntent()
        val text = if (messageEntity.type == MessageType.FILES) {
            context.getString(R.string.files_received)
        } else {
            context.getString(R.string.text_message_received)
        }
        val title = context.getString(R.string.new_message)
        val notification = NotificationCompat.Builder(context, NEW_MESSAGE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()

        notify(notification, contact.address.hashCode())
    }

    fun enableNotifications() {
        _allChatsBlocked.value = false
    }

    fun disableNotifications() {
        _allChatsBlocked.value = true
    }

    fun enterChat(chatId: String?) {
        _currentChat.value = chatId
    }

    fun exitChat() {
        _currentChat.value = null
    }

    fun enterForeground() {
        _isBackground.value = false
    }

    fun enterBackground() {
        _isBackground.value = true
    }
}
