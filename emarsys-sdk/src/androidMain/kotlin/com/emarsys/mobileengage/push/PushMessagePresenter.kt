package com.emarsys.mobileengage.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.emarsys.api.push.PushConstants.DEFAULT_TAP_ACTION_ID
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_ACTION_KEY
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_PAYLOAD_KEY
import com.emarsys.api.push.PushConstants.PUSH_NOTIFICATION_ICON_NAME
import com.emarsys.core.device.AndroidNotificationSettings
import com.emarsys.core.device.PlatformInfoCollector
import com.emarsys.core.resource.MetadataReader
import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.action.models.PushToInappActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.PushToInApp
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PushMessagePresenter(
    private val context: Context,
    private val json: Json,
    private val notificationManager: NotificationManager,
    private val metadataReader: MetadataReader,
    private val notificationCompatStyler: NotificationCompatStyler,
    private val platformInfoCollector: PlatformInfoCollector,
    private val inAppDownloader: InAppDownloaderApi
) : PushPresenter<AndroidPlatformData, AndroidPushMessage> {
    private companion object {
        const val DEBUG_CHANNEL_ID = "ems_debug"
        const val DEBUG_CHANNEL_NAME = "Emarsys SDK Debug Messages"
    }

    override suspend fun present(pushMessage: AndroidPushMessage) {
        val message = handleChannelIdMismatch(
            pushMessage,
            platformInfoCollector.notificationSettings(),
            context
        )

        val iconId = metadataReader.getInt(PUSH_NOTIFICATION_ICON_NAME)
        val channelId = message.data.platformData.channelId
        val collapseId = message.data.platformData.notificationMethod.collapseId
        val tapActionPendingIntent = createTapActionPendingIntent(message)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(iconId)
            .setContentIntent(tapActionPendingIntent)
            .addActions(message)

        notificationCompatStyler.style(notificationBuilder, message)

        notificationManager.notify(collapseId, collapseId.hashCode(), notificationBuilder.build())
    }

    private fun NotificationCompat.Builder.addActions(pushMessage: AndroidPushMessage): NotificationCompat.Builder {
        pushMessage.data.actions?.forEach { actionModel ->
            val actionIntent = createActionIntent(actionModel, pushMessage)
            val action = createNotificationAction(actionModel, actionIntent)

            this.addAction(action)
        }
        return this
    }

    private fun createNotificationAction(
        actionModel: PresentableActionModel,
        actionIntent: Intent
    ): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
            0,
            actionModel.title,
            PendingIntent.getActivity(
                context,
                (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                actionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        ).build()
    }

    private fun createActionIntent(
        actionModel: PresentableActionModel,
        pushMessage: AndroidPushMessage
    ): Intent {
        val intent = Intent(context, NotificationOpenedActivity::class.java)
        intent.action = actionModel.id
        intent.putExtra(
            INTENT_EXTRA_PAYLOAD_KEY,
            json.encodeToString(pushMessage)
        )
        intent.putExtra(
            INTENT_EXTRA_ACTION_KEY,
            json.encodeToString(actionModel)
        )
        return intent
    }

    private suspend fun createTapActionPendingIntent(
        pushMessage: AndroidPushMessage
    ): PendingIntent {
        val intent = Intent(context, NotificationOpenedActivity::class.java)
        intent.action = DEFAULT_TAP_ACTION_ID
        intent.putExtra(
            INTENT_EXTRA_PAYLOAD_KEY,
            json.encodeToString(pushMessage)
        )

        pushMessage.data.defaultTapAction?.let { tapAction ->
            val defaultActionModel = if (tapAction !is PushToInappActionModel) {
                tapAction
            } else {
                pushMessage.data.pushToInApp?.toInternalPushToInappActionModel()
            }

            defaultActionModel?.let {
                intent.putExtra(
                    INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY,
                    json.encodeToString(it)
                )
            }
        }

        return PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private suspend fun PushToInApp.toInternalPushToInappActionModel(): InternalPushToInappActionModel {
        val inappHtml = inAppDownloader.download(this.url)
        return InternalPushToInappActionModel(
            campaignId,
            url,
            inappHtml,
            ignoreViewedEvent
        )
    }

    private fun shouldCreateDebugChannel(
        channelId: String,
        notificationSettings: AndroidNotificationSettings
    ): Boolean {
        return platformInfoCollector.isDebugMode() && !notificationSettings.channelSettings.any { channelSettings -> channelSettings.channelId == channelId }
    }

    private fun handleChannelIdMismatch(
        pushMessage: AndroidPushMessage,
        notificationSettings: AndroidNotificationSettings,
        context: Context
    ): AndroidPushMessage {
        val channelId = pushMessage.data.platformData.channelId
        return if (shouldCreateDebugChannel(channelId, notificationSettings)) {
            val debugChannelId = createDebugChannel(context)
            val platformData = pushMessage.data.platformData.copy(channelId = debugChannelId)
            val pushData = pushMessage.data.copy(platformData = platformData)
            pushMessage.copy(
                body = "DEBUG - channel_id mismatch: $channelId not found!",
                data = pushData,
                title = "Emarsys SDK"
            )
        } else {
            pushMessage
        }
    }

    private fun createDebugChannel(context: Context): String {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            DEBUG_CHANNEL_ID,
            DEBUG_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(notificationChannel)
        return notificationChannel.id
    }
}