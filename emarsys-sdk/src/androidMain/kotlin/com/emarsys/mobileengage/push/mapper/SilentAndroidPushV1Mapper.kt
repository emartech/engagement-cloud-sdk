package com.emarsys.mobileengage.push.mapper

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import com.emarsys.core.mapper.Mapper
import com.emarsys.mobileengage.push.ActionableData
import com.emarsys.mobileengage.push.NotificationOperation
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.NotificationStyle
import com.emarsys.mobileengage.push.model.SilentAndroidPushMessage
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class SilentAndroidPushV1Mapper(
    private val logger: Logger
): Mapper<JsonObject, SilentAndroidPushMessage> {

    override suspend fun map(from: JsonObject): SilentAndroidPushMessage? {
        return try {
            SilentAndroidPushMessage(
                sid = from.getValue("ems.sid").jsonPrimitive.content,
                campaignId = from.getValue("ems.multichannel_id").jsonPrimitive.content,
                platformData = AndroidPlatformData(
                    channelId = from.getValue("notification.channel_id").jsonPrimitive.content,
                    notificationMethod = NotificationMethod(
                        collapseId = from.getValue("ems.notification_method.collapse_key").jsonPrimitive.content,
                        operation = from.getValue("ems.notification_method.operation").jsonPrimitive.content.let { NotificationOperation.valueOf(it) }
                    ),
                    style = from["ems.style"]?.jsonPrimitive?.content?.let { NotificationStyle.valueOf(it) }
                ),
                badgeCount = from["notification.badgeCount"]?.jsonPrimitive?.contentOrNull.fromString(),
                actionableData = ActionableData(
                    actions = from.getValue("ems.actions").jsonPrimitive.content.fromString()
                )
            )
        } catch (e: Exception) {
            logger.error(LogEntry("error", mapOf("reason" to e.localizedMessage)))
            null
        }
    }

}
