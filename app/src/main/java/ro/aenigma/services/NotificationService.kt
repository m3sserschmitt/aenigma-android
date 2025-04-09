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
import ro.aenigma.data.database.extensions.MessageEntityExtensions.getMessageTextByAction
import ro.aenigma.ui.AppActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val NEW_MESSAGE_CHANNEL_ID = "new-message-channel"
        private const val NEW_MESSAGE_CHANNEL_NAME = "New message notifications"
        private const val NEW_MESSAGE_CHANNEL_DESCRIPTION =
            "Channel used to display notification related to incoming messages"

        const val NOTIFICATIONS_DISABLE_ALL = "*"
    }

    private fun createBasicNotification(
        title: String,
        text: String,
        icon: Int,
        channel: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            )
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

    private val _blockedNotificationsSource: MutableLiveData<String> = MutableLiveData("")

    val blockedNotificationsSource: LiveData<String> = _blockedNotificationsSource

    fun notify(contact: ContactEntity, messageEntity: MessageEntity) {
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
        val notification = createBasicNotification(
            contact.name.toString(), messageEntity.getMessageTextByAction(context),
            R.drawable.ic_message,
            NEW_MESSAGE_CHANNEL_ID
        )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(contact.address.hashCode(), notification)
            }
        }
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
