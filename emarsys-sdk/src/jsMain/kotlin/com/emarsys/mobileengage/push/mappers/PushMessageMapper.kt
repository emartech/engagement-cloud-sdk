package com.emarsys.mobileengage.push.mappers

import com.emarsys.core.log.Logger
import com.emarsys.core.mapper.Mapper
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import com.emarsys.mobileengage.inapp.PushToInApp
import com.emarsys.mobileengage.push.ActionableData
import com.emarsys.mobileengage.push.DisplayableData
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

            val defaultTapAction = remoteWebPushMessage.messageData.notificationSettings.link?.let {
                        BasicOpenExternalUrlActionModel(it)
                    }
            val actions: List<PresentableActionModel>? = remoteWebPushMessage.messageData.notificationSettings.actions?.map { remoteWebPushAction ->
                        PresentableOpenExternalUrlActionModel(
                            remoteWebPushAction.id,
                            remoteWebPushAction.title,
                            remoteWebPushAction.url
                        )
                    }
            val pushToInApp: PushToInApp? = remoteWebPushMessage.messageData.inApp
            val actionableData = if (actions != null || defaultTapAction != null || pushToInApp != null) {
                ActionableData(
                    actions = actions,
                    defaultTapAction = defaultTapAction,
                    pushToInApp = pushToInApp
                )
            } else null

            return JsPushMessage(
                remoteWebPushMessage.messageData.sid,
                DEFAULT_CAMPAIGN_ID,
                JsPlatformData(remoteWebPushMessage.messageData.applicationCode),
                remoteWebPushMessage.messageData.notificationSettings.badgeCount,
                actionableData = actionableData,
                DisplayableData(remoteWebPushMessage.title ?: DEFAULT_TITLE,
                    remoteWebPushMessage.message,
                    iconUrlString = remoteWebPushMessage.messageData.notificationSettings.icon,
                    imageUrlString = remoteWebPushMessage.messageData.notificationSettings.image)
            )
        } catch (exception: Exception) {
            logger.error("WebPushMessageMapper", exception)
            null
        }
    }
}