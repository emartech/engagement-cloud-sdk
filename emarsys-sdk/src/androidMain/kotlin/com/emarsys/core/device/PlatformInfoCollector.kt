package com.emarsys.core.device

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.util.DisplayMetrics

class PlatformInfoCollector(
    private val context: Context,
) : PlatformInfoCollectorApi {

    override fun notificationSettings(): AndroidNotificationSettings {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return AndroidNotificationSettings(
            notificationManager.areNotificationsEnabled(),
            notificationManager.importance,
            parseChannelSettings(notificationManager.notificationChannels)
        )
    }

    override fun displayMetrics(): DisplayMetrics? {
        return Resources.getSystem().displayMetrics
    }

    override fun isDebugMode(): Boolean {
        return 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
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