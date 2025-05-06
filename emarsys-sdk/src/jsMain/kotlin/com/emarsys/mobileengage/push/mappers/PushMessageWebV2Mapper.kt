package com.emarsys.mobileengage.push.mappers

import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.LogLevel
import com.emarsys.core.mapper.Mapper
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.inapp.PushToInAppPayload
import com.emarsys.mobileengage.push.ActionableData
import com.emarsys.mobileengage.push.DisplayableData
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.mobileengage.push.model.v1.RemoteWebPushMessageV2
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class PushMessageWebV2Mapper(
    private val json: Json,
    private val logger: ConsoleLogger
) : Mapper<String, JsPushMessage> {

    override suspend fun map(from: String): JsPushMessage? {
        return try {
            val remoteMessage = json.decodeFromString<RemoteWebPushMessageV2>(from)

            val defaultTapAction = remoteMessage.notification.defaultAction
            val actions: List<PresentableActionModel>? = remoteMessage.notification.actions
            val pushToInAppPayload: PushToInAppPayload? = null
            val actionableData =
                if (actions != null || defaultTapAction != null || pushToInAppPayload != null) {
                    ActionableData(
                        actions = actions,
                        defaultTapAction = defaultTapAction
                    )
                } else null

            return JsPushMessage(
                remoteMessage.ems.trackingInfo,
                JsPlatformData,
                remoteMessage.notification.badgeCount,
                actionableData,
                DisplayableData(
                    remoteMessage.notification.title,
                    remoteMessage.notification.body,
                    iconUrlString = remoteMessage.notification.icon,
                    imageUrlString = remoteMessage.notification.imageUrl
                )
            )
        } catch (exception: Exception) {
            logger.logToConsole(
                "WebPushMessageV2Mapper",
                LogLevel.Error,
                "Error mapping push message",
                exception,
                JsonObject(emptyMap())
            )
            null
        }
    }
}