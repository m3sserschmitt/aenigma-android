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
import ro.aenigma.ui.navigation.Screens
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Notifier @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        const val TOR_NOTIFICATION_ID = 1371
        const val NOTIFICATION_SERVICE_NOTIFICATION_ID = 2173

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
        private const val NOTIFICATION_SERVICE_CHANNEL_ID = "notification-service-channel"
        private const val NOTIFICATION_SERVICE_CHANNEL_NAME = "Notification Service"
        private const val NOTIFICATION_SERVICE_CHANNEL_DESCRIPTION =
            "Channel used by the Notification Service"
    }

    private fun createNotificationChannel(
        channel: String,
        name: String,
        channelDescription: String,
        importance: Int
    ) {
        val notificationChannel =
            NotificationChannel(channel, name, importance).apply {
                description = channelDescription
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun createChatNavigationIntent(chatId: String): PendingIntent {
        return PendingIntent.getActivity(
            context,
            0,
            Intent(
                Intent.ACTION_VIEW,
                Screens.getChatDeepLink(chatId),
                context,
                AppActivity::class.java
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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
            TOR_SERVICE_CHANNEL_ID,
            TOR_SERVICE_CHANNEL_NAME,
            TOR_SERVICE_CHANNEL_DESCRIPTION,
            NotificationManager.IMPORTANCE_LOW
        )
        return NotificationCompat.Builder(context, TOR_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vpn)
            .setContentTitle(context.getString(R.string.tor_service))
            .setContentText(text)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    fun createWorkerNotification(text: String): Notification {
        createNotificationChannel(
            WORKERS_CHANNEL_ID,
            WORKERS_CHANNEL_NAME,
            WORKERS_CHANNEL_DESCRIPTION,
            NotificationManager.IMPORTANCE_LOW
        )
        return NotificationCompat.Builder(context, WORKERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_api)
            .setContentTitle(context.getString(R.string.background_work))
            .setContentText(text)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    fun createNotificationServiceNotification(): Notification {
        createNotificationChannel(
            NOTIFICATION_SERVICE_CHANNEL_ID,
            NOTIFICATION_SERVICE_CHANNEL_NAME,
            NOTIFICATION_SERVICE_CHANNEL_DESCRIPTION,
            NotificationManager.IMPORTANCE_LOW
        )
        return NotificationCompat.Builder(context, NOTIFICATION_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_service))
            .setContentText(context.getString(R.string.live_notifications_on))
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    fun notifyUploadProgress(progress: Int, id: Int) {
        notify(createWorkerNotification(context.getString(R.string.uploading).format(progress)), id)
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
            NEW_MESSAGE_CHANNEL_DESCRIPTION,
            NotificationManager.IMPORTANCE_HIGH
        )
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
            .setContentIntent(createChatNavigationIntent(contact.address))
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
        NotificationManagerCompat.from(context).cancel(chatId.hashCode())
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
