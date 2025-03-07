package com.emarsys.mobileengage.push.mappers

import com.emarsys.core.log.Logger
import com.emarsys.core.mapper.Mapper
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.inapp.PushToInApp
import com.emarsys.mobileengage.push.ActionableData
import com.emarsys.mobileengage.push.DisplayableData
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.mobileengage.push.model.v1.RemoteWebPushMessageV2
import kotlinx.serialization.json.Json

class PushMessageWebV1Mapper(
    private val json: Json,
    private val logger: Logger
) : Mapper<String, JsPushMessage> {

    override suspend fun map(from: String): JsPushMessage? {
        return try {
            val remoteMessage = json.decodeFromString<RemoteWebPushMessageV2>(from)

            val defaultTapAction = remoteMessage.notification.defaultAction
            val actions: List<PresentableActionModel>? = remoteMessage.notification.actions
            val pushToInApp: PushToInApp? = null
            val actionableData = if (actions != null || defaultTapAction != null || pushToInApp != null) {
                ActionableData(
                    actions = actions,
                    defaultTapAction = defaultTapAction,
                    pushToInApp = pushToInApp
                )
            } else null

            return JsPushMessage(
                remoteMessage.ems.sid,
                remoteMessage.ems.campaignId,
                JsPlatformData(remoteMessage.ems.applicationCode),
                remoteMessage.notification.badgeCount,
                actionableData = actionableData,
                DisplayableData(remoteMessage.notification.title,
                    remoteMessage.notification.body,
                    iconUrlString = remoteMessage.notification.icon,
                    imageUrlString = remoteMessage.notification.imageUrl)
            )
        } catch (exception: Exception) {
            logger.error("WebPushMessageV1Mapper", exception)
            null
        }
    }
}