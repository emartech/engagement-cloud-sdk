package com.emarsys.mobileengage.push

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
import com.emarsys.core.resource.MetadataReader
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PushMessagePresenter(
    private val context: Context,
    private val json: Json,
    private val notificationManager: NotificationManager,
    private val metadataReader: MetadataReader
) : PushPresenter<AndroidPlatformData, AndroidPushMessage> {

    override suspend fun present(pushMessage: AndroidPushMessage) {
        val iconId = metadataReader.getInt(PUSH_NOTIFICATION_ICON_NAME)
        val channelId = pushMessage.data.platformData.channelId
        val collapseId = pushMessage.data.platformData.notificationMethod.collapseId
        val tapActionPendingIntent = createTapActionPendingIntent(pushMessage)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(pushMessage.title)
            .setContentText(pushMessage.body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(iconId)
            .setContentIntent(tapActionPendingIntent)
            .addActions(pushMessage)

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

    private fun createTapActionPendingIntent(
        pushMessage: AndroidPushMessage
    ): PendingIntent {
        val intent = Intent(context, NotificationOpenedActivity::class.java)
        intent.action = DEFAULT_TAP_ACTION_ID
        intent.putExtra(
            INTENT_EXTRA_PAYLOAD_KEY,
            json.encodeToString(pushMessage)
        )

        pushMessage.data.defaultTapAction?.let {
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
}