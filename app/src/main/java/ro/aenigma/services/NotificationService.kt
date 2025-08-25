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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import ro.aenigma.R
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.activities.AppActivity
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
        private const val TOR_SERVICE_CHANNEL_DESCRIPTION = "Channel used for Tor Foreground Service"
        private const val TOR_SERVICE_NOTIFICATION_TITLE = "Tor"

        private const val WORKERS_CHANNEL_ID = "workers-service-channel"
        private const val WORKERS_CHANNEL_NAME = "Background Worker Notification"
        private const val WORKERS_CHANNEL_DESCRIPTION = "Channel used for Background workers"
        private const val WORKERS_NOTIFICATION_TITLE = "Background work"

        private const val NEW_MESSAGE_CHANNEL_ID = "new-message-channel"
        private const val NEW_MESSAGE_CHANNEL_NAME = "New message notifications"
        private const val NEW_MESSAGE_CHANNEL_DESCRIPTION =
            "Channel used to display notification related to incoming messages"

        const val NOTIFICATIONS_DISABLE_ALL = "*"
    }

    private fun createNotificationChannel(
        channel: String,
        name: String,
        description: String,
        importance: Int
    ) {
        val notificationChannel = NotificationChannel(channel, name, importance).apply {
            this.description = description
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
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

    private val _blockedNotificationsSource: MutableLiveData<String> = MutableLiveData("")

    val blockedNotificationsSource: LiveData<String> = _blockedNotificationsSource

    fun createTorServiceNotification(text: String): Notification {
        createNotificationChannel(
            TOR_SERVICE_CHANNEL_ID, TOR_SERVICE_CHANNEL_NAME, TOR_SERVICE_CHANNEL_DESCRIPTION,
            NotificationManager.IMPORTANCE_MIN
        )
        return NotificationCompat.Builder(context, TOR_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vpn)
            .setContentTitle(TOR_SERVICE_NOTIFICATION_TITLE)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            ).setSilent(true)
            .build()
    }

    fun createWorkerNotification(text: String): Notification {
        createNotificationChannel(
            WORKERS_CHANNEL_ID, WORKERS_CHANNEL_NAME, WORKERS_CHANNEL_DESCRIPTION,
            NotificationManager.IMPORTANCE_MIN
        )
        return NotificationCompat.Builder(context, WORKERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_api)
            .setContentTitle(WORKERS_NOTIFICATION_TITLE)
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

    fun notifyNewMessage(contact: ContactEntity, messageEntity: MessageEntity) {
        if (listOf(NOTIFICATIONS_DISABLE_ALL, contact.address)
                .contains(blockedNotificationsSource.value)
        ) {
            return
        }

        createNotificationChannel(
            NEW_MESSAGE_CHANNEL_ID,
            NEW_MESSAGE_CHANNEL_NAME,
            NEW_MESSAGE_CHANNEL_DESCRIPTION,
            NotificationManager.IMPORTANCE_MAX
        )
        val intent = createChatNavigationIntent()
        val text = if(messageEntity.type == MessageType.FILES) {
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

    fun dismissNotifications(address: String) {
        with(NotificationManagerCompat.from(context))
        {
            cancel(address.hashCode())
        }
    }

    fun enableNotifications() {
        _blockedNotificationsSource.postValue("")
    }

    fun disableNotifications() {
        _blockedNotificationsSource.postValue(NOTIFICATIONS_DISABLE_ALL)
    }

    fun disableNotifications(address: String) {
        _blockedNotificationsSource.postValue(address)
    }
}
