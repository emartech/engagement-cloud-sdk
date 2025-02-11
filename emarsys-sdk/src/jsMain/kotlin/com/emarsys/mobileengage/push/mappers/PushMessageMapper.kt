package com.emarsys.mobileengage.push.mappers

import com.emarsys.core.log.Logger
import com.emarsys.core.mapper.Mapper
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import com.emarsys.mobileengage.push.PresentablePushData
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.mobileengage.push.model.RemoteWebPushMessage
import kotlinx.serialization.json.Json

class PushMessageMapper(private val json: Json, private val logger: Logger) :
    Mapper<String, JsPushMessage> {
    private companion object {
        const val DEFAULT_CAMPAIGN_ID = "DefaultCampaignId"
        const val DEFAULT_TITLE = "DefaultTitle"
    }

    override suspend fun map(from: String): JsPushMessage? {
        return try {
            val remoteWebPushMessage = json.decodeFromString<RemoteWebPushMessage>(from)

            return JsPushMessage(
                remoteWebPushMessage.messageData.id,
                remoteWebPushMessage.title ?: DEFAULT_TITLE,
                remoteWebPushMessage.message,
                remoteWebPushMessage.messageData.notificationSettings.icon,
                remoteWebPushMessage.messageData.notificationSettings.image,
                PresentablePushData(
                    sid = remoteWebPushMessage.messageData.sid,
                    campaignId = DEFAULT_CAMPAIGN_ID,
                    actions = remoteWebPushMessage.messageData.notificationSettings.actions?.map { remoteWebPushAction ->
                        PresentableOpenExternalUrlActionModel(
                            remoteWebPushAction.id,
                            remoteWebPushAction.title,
                            remoteWebPushAction.url
                        )
                    },
                    platformData = JsPlatformData(remoteWebPushMessage.messageData.applicationCode),
                    pushToInApp = remoteWebPushMessage.messageData.inApp,
                    defaultTapAction = remoteWebPushMessage.messageData.notificationSettings.link?.let {
                        BasicOpenExternalUrlActionModel(it)
                    },
                    badgeCount = remoteWebPushMessage.messageData.notificationSettings.badgeCount
                )
            )
        } catch (exception: Exception) {
            logger.error("WebPushMessageMapper", exception)
            null
        }
    }
}