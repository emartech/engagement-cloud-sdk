package com.emarsys.mobileengage.push.mapper

import com.emarsys.core.log.Logger
import com.emarsys.core.mapper.Mapper
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.push.ActionableData
import com.emarsys.mobileengage.push.DisplayableData
import com.emarsys.mobileengage.push.NotificationOperation
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.NotificationStyle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class HuaweiPushV2Mapper(
    private val uuidProvider: UuidProviderApi,
    private val logger: Logger,
    private val json: Json
) : Mapper<JsonObject, AndroidPushMessage> {

    private companion object {
        const val DEFAULT_CHANNEL_ID = "Missing Channel Id"
        const val TITLE = "title"
        const val BODY = "body"
        const val ICON = "icon"
        const val IMAGE_URL = "imageUrl"
        const val ACTIONS = "actions"
        const val DEFAULT_ACTION = "defaultAction"
        const val EMS = "ems"
        const val NOTIFICATION = "notification"
        const val TRACKING_INFO = "trackingInfo"
        const val CHANNEL_ID = "channelId"
        const val COLLAPSE_ID = "collapseId"
        const val OPERATION = "operation"
        const val STYLE = "style"
        const val BADGE_COUNT = "badgeCount"
        const val METHOD = "method"
        const val VALUE = "value"
    }

    override suspend fun map(from: JsonObject): AndroidPushMessage? {
        return try {
            val emsObject = from[EMS]?.jsonObject
                ?: throw Exception("ems object missing from push payload: $from")
            val notificationObject = from[NOTIFICATION]?.jsonObject
                ?: throw Exception("notification object missing from push payload: $from")
            val defaultTapAction: BasicActionModel? =
                notificationObject[DEFAULT_ACTION]?.jsonObject?.let { json.decodeFromJsonElement(it) }
            val actions: List<PresentableActionModel>? =
                notificationObject[ACTIONS]?.jsonArray?.let { json.decodeFromJsonElement(it) }
            val actionableData = ActionableData(actions, defaultTapAction)

            val trackingInfo = emsObject[TRACKING_INFO]?.jsonPrimitive?.contentOrNull ?: "{}"

            AndroidPushMessage(
                trackingInfo = trackingInfo,
                platformData = AndroidPlatformData(
                    channelId = notificationObject[CHANNEL_ID].getStringOrDefault(DEFAULT_CHANNEL_ID),
                    NotificationMethod(
                        collapseId = notificationObject[COLLAPSE_ID].getStringOrDefault(uuidProvider.provide()),
                        operation = NotificationOperation.valueOf(
                            notificationObject[OPERATION].getStringOrDefault("INIT")
                        )
                    ),
                    style = notificationObject[STYLE]?.jsonPrimitive?.contentOrNull?.let {
                        NotificationStyle.valueOf(it.uppercase())
                    }
                ),
                badgeCount = notificationObject[BADGE_COUNT]?.jsonObject?.let { badgeCount ->
                    BadgeCount(
                        method = badgeCount.getValue(METHOD).jsonPrimitive.content.let {
                            BadgeCountMethod.valueOf(it.uppercase())
                        },
                        value = badgeCount.getValue(VALUE).jsonPrimitive.int
                    )
                },
                displayableData = DisplayableData(
                    title = notificationObject.getValue(TITLE).jsonPrimitive.contentOrNull!!,
                    body = notificationObject.getValue(BODY).jsonPrimitive.contentOrNull!!,
                    iconUrlString = notificationObject[ICON]?.jsonPrimitive?.contentOrNull,
                    imageUrlString = notificationObject[IMAGE_URL]?.jsonPrimitive?.contentOrNull
                ),
                actionableData = actionableData
            )
        } catch (exception: Exception) {
            println(exception)
            logger.error("push mapping failed", exception)
            null
        }
    }
}

fun JsonElement?.getStringOrDefault(default: String): String {
    return this?.jsonPrimitive?.contentOrNull ?: default
}