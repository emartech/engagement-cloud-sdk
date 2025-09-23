package com.emarsys.core.device.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.emarsys.core.device.ChannelSettings

class AndroidNotificationSettingsCollector(
    private val context: Context,
) : AndroidNotificationSettingsCollectorApi {

    override fun collect(): AndroidNotificationSettings {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return AndroidNotificationSettings(
            notificationManager.areNotificationsEnabled(),
            notificationManager.importance,
            parseChannelSettings(notificationManager.notificationChannels)
        )
    }

    private fun parseChannelSettings(notificationChannels: List<NotificationChannel>): List<ChannelSettings> {
        return notificationChannels.map { channel ->
            ChannelSettings(
                channelId = channel.id,
                importance = channel.importance,
                canBypassDnd = channel.canBypassDnd(),
                canShowBadge = channel.canShowBadge(),
                shouldVibrate = channel.shouldVibrate(),
                shouldShowLights = channel.shouldShowLights()
            )
        }
    }
}