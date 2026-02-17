package com.sap.ec.mobileengage.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.sap.ec.api.push.PushConstants.DEFAULT_TAP_ACTION_ID
import com.sap.ec.api.push.PushConstants.INTENT_EXTRA_ACTION_KEY
import com.sap.ec.api.push.PushConstants.INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY
import com.sap.ec.api.push.PushConstants.INTENT_EXTRA_PAYLOAD_KEY
import com.sap.ec.api.push.PushConstants.PUSH_NOTIFICATION_ICON_NAME
import com.sap.ec.core.device.PlatformInfoCollectorApi
import com.sap.ec.core.device.notification.AndroidNotificationSettings
import com.sap.ec.core.device.notification.AndroidNotificationSettingsCollectorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.resource.MetadataReader
import com.sap.ec.mobileengage.action.models.DismissActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.mobileengage.push.model.AndroidPlatformData
import com.sap.ec.mobileengage.push.model.AndroidPushMessage
import kotlinx.serialization.json.Json

internal class PushMessagePresenter(
    private val context: Context,
    private val json: Json,
    private val notificationManager: NotificationManager,
    private val metadataReader: MetadataReader,
    private val notificationCompatStyler: NotificationCompatStyler,
    private val platformInfoCollector: PlatformInfoCollectorApi,
    private val androidNotificationSettingsCollector: AndroidNotificationSettingsCollectorApi,
    private val sdkLogger: Logger
) : PushPresenter<AndroidPlatformData, AndroidPushMessage> {
    private companion object {
        const val DEBUG_CHANNEL_ID = "ems_debug"
        const val DEBUG_CHANNEL_NAME = "Engagement Cloud SDK Debug Messages"
    }

    override suspend fun present(pushMessage: AndroidPushMessage) {
        val message = handleChannelIdMismatch(
            pushMessage,
            androidNotificationSettingsCollector.collect(),
            context
        )

        val iconId = metadataReader.getInt(PUSH_NOTIFICATION_ICON_NAME)
        val channelId = message.platformData.channelId
        val collapseId = message.platformData.notificationMethod.collapseId
        val tapActionPendingIntent = createTapActionPendingIntent(message)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(message.displayableData.title)
            .setContentText(message.displayableData.body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(iconId)
            .setContentIntent(tapActionPendingIntent)
            .setBadgeCount(message)
            .addActions(message)

        notificationCompatStyler.style(notificationBuilder, message)
        sdkLogger.debug("Notification created and displayed")
        notificationManager.notify(collapseId, collapseId.hashCode(), notificationBuilder.build())
    }

    private fun NotificationCompat.Builder.addActions(pushMessage: AndroidPushMessage): NotificationCompat.Builder {
        pushMessage.actionableData?.actions?.forEach { actionModel ->
            if (actionModel is DismissActionModel) {
                actionModel.dismissId = pushMessage.platformData.notificationMethod.collapseId
            }
            val actionIntent = createActionIntent(actionModel, pushMessage)
            val action = createNotificationAction(actionModel, actionIntent)

            this.addAction(action)
        }
        return this
    }

    private fun NotificationCompat.Builder.setBadgeCount(pushMessage: AndroidPushMessage): NotificationCompat.Builder {
        pushMessage.badgeCount?.let { badgeCount ->
            this.setNumber(badgeCount.value)
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
        intent.setPackage(context.packageName)
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

    private fun createTapActionPendingIntent(
        pushMessage: AndroidPushMessage
    ): PendingIntent {
        val intent = Intent(context, NotificationOpenedActivity::class.java)
        intent.setPackage(context.packageName)
        intent.action = DEFAULT_TAP_ACTION_ID
        intent.putExtra(
            INTENT_EXTRA_PAYLOAD_KEY,
            json.encodeToString(pushMessage)
        )

        pushMessage.actionableData?.defaultTapAction?.let {
            intent.putExtra(
                INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY,
                json.encodeToString(it)
            )
        }

        return PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
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
        val channelId = pushMessage.platformData.channelId
        return if (shouldCreateDebugChannel(channelId, notificationSettings)) {
            val debugChannelId = createDebugChannel(context)
            val platformData = pushMessage.platformData.copy(channelId = debugChannelId)
            val displayableData = pushMessage.displayableData.copy(
                title = "Engagement Cloud SDK",
                body = "DEBUG - channel_id mismatch: $channelId not found!",
            )
            pushMessage.copy(platformData = platformData, displayableData = displayableData)
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