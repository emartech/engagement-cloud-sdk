package com.emarsys.mobileengage.push

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import com.emarsys.api.push.PushConstants.PUSH_NOTIFICATION_ICON_NAME
import com.emarsys.applicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class PushMessagePresenter(
    private val context: Context,
    private val json: Json,
    private val notificationManager: NotificationManager
) : PushPresenter {
    override suspend fun present(pushMessage: PushMessage) {
        val iconId = getIconId()
        val channelId = extractChannelId(pushMessage)

        channelId?.let {
            val notificationBuilder = NotificationCompat.Builder(context, it)
                .setContentTitle(pushMessage.title)
                .setContentText(pushMessage.body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(iconId)

            notificationManager.notify(123, notificationBuilder.build())
        }
    }

    private fun extractChannelId(pushMessage: PushMessage): String? {
        return pushMessage.data?.platformContext?.let {
            val platformContext = json.decodeFromJsonElement<Map<String, String>>(it)
            platformContext.getOrDefault("channelId", null)
        }
    }

    private fun getIconId(): Int {
        val applicationInfo = applicationContext.packageManager.getApplicationInfo(
            applicationContext.packageName,
            PackageManager.GET_META_DATA
        )
        return applicationInfo.metaData.getInt(PUSH_NOTIFICATION_ICON_NAME)
    }
}