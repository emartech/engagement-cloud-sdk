package com.emarsys.mobileengage.push.mapper

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
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AndroidPushV2Mapper(
    private val logger: Logger,
    private val json: Json
): Mapper<JsonObject, AndroidPushMessage> {

    companion object {
        private const val DEFAULT_CHANNEL_ID = "Missing Channel Id"
        private const val TITLE = "notification.title"
        private const val BODY = "notification.body"
        private const val ICON = "notification.icon"
        private const val IMAGE_URL = "notification.imageUrl"
        private const val STYLE = "notification.style"
        private const val CHANNEL_ID = "notification.channelId"
        private const val COLLAPSE_ID = "notification.collapseId"
        private const val OPERATION = "notification.operation"
        private const val DEFAULT_ACTION = "notification.defaultAction"
        private const val ACTIONS = "notification.actions"
        private const val IN_APP = "notification.inapp"
        private const val BADGE_COUNT = "notification.badgeCount"
        private const val EMS = "ems"
    }

    override suspend fun map(from: JsonObject): AndroidPushMessage? {
        return try {
            val ems: JsonObject = from.getValue(EMS).jsonPrimitive.content.fromString(json)!!
            val treatments: JsonObject? = ems["treatments"]?.jsonObject

            val defaultTapAction: BasicActionModel? = from[DEFAULT_ACTION]?.jsonPrimitive?.contentOrNull.fromString(json)
            val actions: List<PresentableActionModel>? = from[ACTIONS]?.jsonPrimitive?.contentOrNull.fromString(json)
            val pushToInApp: PushToInApp? = from[IN_APP]?.jsonPrimitive?.contentOrNull.fromString(json)

            val actionableData = if (actions != null || defaultTapAction != null || pushToInApp != null) {
                ActionableData(
                    actions = actions,
                    defaultTapAction = defaultTapAction,
                    pushToInApp = pushToInApp
                )
            } else null

            AndroidPushMessage(
                sid = treatments?.getValue("sid")?.jsonPrimitive?.content ?: "",
                campaignId = ems.getValue("campaignId").jsonPrimitive.content,
                platformData = AndroidPlatformData(
                    channelId = from[CHANNEL_ID]?.jsonPrimitive?.contentOrNull ?: DEFAULT_CHANNEL_ID,
                    notificationMethod = NotificationMethod(
                        collapseId = from.getValue(COLLAPSE_ID).jsonPrimitive.content,
                        operation = from.getValue(OPERATION).jsonPrimitive.content.let { NotificationOperation.valueOf(it) }
                    ),
                    style = from[STYLE]?.jsonPrimitive?.contentOrNull?.let { NotificationStyle.valueOf(it) }
                ),
                badgeCount = from[BADGE_COUNT]?.jsonPrimitive?.contentOrNull.fromString(json),
                displayableData = DisplayableData(
                    from.getValue(TITLE).jsonPrimitive.content,
                    from.getValue(BODY).jsonPrimitive.content,
                    iconUrlString = from[ICON]?.jsonPrimitive?.contentOrNull,
                    imageUrlString = from[IMAGE_URL]?.jsonPrimitive?.contentOrNull),
                actionableData = actionableData
            )
        } catch (e: Exception) {
            logger.error("AndroidPushV2Mapper", e)
            null
        }
    }
}

inline fun <reified T> String?.fromString(json: Json): T? {
    return this?.let { json.decodeFromString<T>(it) }
}