package com.emarsys.mobileengage.push.mapper

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import com.emarsys.core.mapper.Mapper
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.inapp.PushToInApp
import com.emarsys.mobileengage.push.ActionableData
import com.emarsys.mobileengage.push.DisplayableData
import com.emarsys.mobileengage.push.NotificationOperation
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.NotificationStyle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

class AndroidPushV1Mapper(
    private val logger: Logger): Mapper<JsonObject, AndroidPushMessage> {

    override suspend fun map(from: JsonObject): AndroidPushMessage? {
        return try {
            val defaultTapAction = extractDefaultAction(from)
            val actions: List<PresentableActionModel>? = from["ems.actions"]?.jsonPrimitive?.contentOrNull.fromString()
            val pushToInApp: PushToInApp? = from["ems.inapp"]?.jsonPrimitive?.contentOrNull.fromString()
            val actionableData = if (actions != null || defaultTapAction != null || pushToInApp != null) {
                ActionableData(
                    actions = actions,
                    defaultTapAction = defaultTapAction,
                    pushToInApp = pushToInApp
                )
            } else null

            AndroidPushMessage(
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
                displayableData = DisplayableData(
                    from.getValue("notification.title").jsonPrimitive.content,
                    from.getValue("notification.body").jsonPrimitive.content,
                    iconUrlString = from["notification.icon"]?.jsonPrimitive?.contentOrNull,
                    imageUrlString = from["notification.image"]?.jsonPrimitive?.contentOrNull),
                actionableData = actionableData
            )
        } catch (e: Exception) {
            logger.error(LogEntry("error", mapOf("reason" to e.localizedMessage)))
            null
        }
    }

    private fun extractDefaultAction(remoteMessageData: JsonObject?): BasicActionModel? {
        return remoteMessageData?.let {
            return if (it["ems.tap_actions.default_action.type"] == null) {
                null
            } else {
                buildJsonObject {
                    put("type", it.getValue("ems.tap_actions.default_action.type").jsonPrimitive)
                    put("name", it.getValue("ems.tap_actions.default_action.name").jsonPrimitive)
                    it["ems.tap_actions.default_action.url"]?.jsonPrimitive?.let { url ->
                        put("url", url)
                    }
                    it["ems.tap_actions.default_action.payload"]?.jsonPrimitive?.let { payload ->
                        val jsonPayload: JsonObject = Json.decodeFromString(payload.content)
                        put("payload", jsonPayload)
                    }
                }.let { defaultJson ->
                    Json.decodeFromJsonElement(defaultJson)
                }
            }
        }
    }
}
