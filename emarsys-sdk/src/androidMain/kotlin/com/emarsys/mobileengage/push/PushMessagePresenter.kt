package com.emarsys.mobileengage.push

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import com.emarsys.api.push.PushConstants.PUSH_NOTIFICATION_ICON_NAME
import com.emarsys.applicationContext
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import kotlinx.serialization.json.Json

class PushMessagePresenter(
    private val context: Context,
    private val json: Json,
    private val notificationManager: NotificationManager,
) : PushPresenter<AndroidPlatformData, AndroidPushMessage> {

    override suspend fun present(pushMessage: AndroidPushMessage) {
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

    private fun extractChannelId(pushMessage: AndroidPushMessage): String? {
        return pushMessage.data?.platformData?.channelId
    }

    private fun getIconId(): Int {
        val applicationInfo = applicationContext.packageManager.getApplicationInfo(
            applicationContext.packageName,
            PackageManager.GET_META_DATA
        )
        return applicationInfo.metaData.getInt(PUSH_NOTIFICATION_ICON_NAME)
    }
}