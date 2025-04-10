package com.emarsys.mobileengage.push.mapper

import com.emarsys.core.log.Logger
import com.emarsys.core.mapper.Mapper
import com.emarsys.mobileengage.push.ActionableData
import com.emarsys.mobileengage.push.NotificationOperation
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.NotificationStyle
import com.emarsys.mobileengage.push.model.SilentAndroidPushMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class SilentAndroidPushV2Mapper(
    private val logger: Logger,
    private val json: Json
) : Mapper<JsonObject, SilentAndroidPushMessage> {

    companion object {
        private const val DEFAULT_CHANNEL_ID = "Missing Channel Id"
        private const val STYLE = "notification.style"
        private const val CHANNEL_ID = "notification.channelId"
        private const val COLLAPSE_ID = "notification.collapseId"
        private const val OPERATION = "notification.operation"
        private const val ACTIONS = "notification.actions"
        private const val BADGE_COUNT = "notification.badgeCount"
        private const val EMS = "ems"
        private const val TRACKING_INFO = "trackingInfo"
    }

    override suspend fun map(from: JsonObject): SilentAndroidPushMessage? {
        return try {
            val ems: JsonObject = from.getValue(EMS).jsonPrimitive.content.fromString(json)!!

            SilentAndroidPushMessage(
                trackingInfo = ems.getValue(TRACKING_INFO).jsonPrimitive.content,
                platformData = AndroidPlatformData(
                    channelId = from[CHANNEL_ID]?.jsonPrimitive?.contentOrNull
                        ?: DEFAULT_CHANNEL_ID,
                    notificationMethod = NotificationMethod(
                        collapseId = from.getValue(COLLAPSE_ID).jsonPrimitive.content,
                        operation = from.getValue(OPERATION).jsonPrimitive.content.let {
                            NotificationOperation.valueOf(
                                it
                            )
                        }
                    ),
                    style = from[STYLE]?.jsonPrimitive?.contentOrNull?.let {
                        NotificationStyle.valueOf(
                            it
                        )
                    }
                ),
                badgeCount = from[BADGE_COUNT]?.jsonPrimitive?.contentOrNull.fromString(json),
                actionableData = ActionableData(
                    actions = from.getValue(ACTIONS).jsonPrimitive.content.fromString(json),
                )
            )
        } catch (e: Exception) {
            logger.error("push mapping failed", e)
            null
        }
    }
}
