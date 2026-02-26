package com.sap.ec.mobileengage.push.mapper

import com.sap.ec.core.log.Logger
import com.sap.ec.core.mapper.Mapper
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.mobileengage.action.models.BadgeCount
import com.sap.ec.mobileengage.action.models.BadgeCountMethod
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.push.ActionableData
import com.sap.ec.mobileengage.push.NotificationOperation
import com.sap.ec.mobileengage.push.model.AndroidPlatformData
import com.sap.ec.mobileengage.push.model.NotificationMethod
import com.sap.ec.mobileengage.push.model.NotificationStyle
import com.sap.ec.mobileengage.push.model.SilentAndroidPushMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class SilentHuaweiPushV2Mapper(
    private val uuidProvider: UuidProviderApi,
    private val logger: Logger,
    private val json: Json
) : Mapper<JsonObject, SilentAndroidPushMessage> {

    private companion object {
        const val DEFAULT_CHANNEL_ID = "Missing Channel Id"
        const val ACTIONS = "actions"
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

    override suspend fun map(from: JsonObject): SilentAndroidPushMessage? {
        return try {
            val emsObject = from[EMS]?.jsonObject
                ?: throw Exception("ems object missing from push payload: $from")
            val notificationObject = from[NOTIFICATION]?.jsonObject
                ?: throw Exception("notification object missing from push payload: $from")

            val actions: List<BasicActionModel>? =
                notificationObject[ACTIONS]?.jsonArray?.let { json.decodeFromJsonElement(it) }
            val actionableData = ActionableData(actions)

            val trackingInfo = emsObject[TRACKING_INFO]?.jsonPrimitive?.contentOrNull ?: "{}"

            SilentAndroidPushMessage(
                trackingInfo = trackingInfo,
                platformData = AndroidPlatformData(
                    channelId = notificationObject[CHANNEL_ID].getStringOrDefault(DEFAULT_CHANNEL_ID),
                    notificationMethod = NotificationMethod(
                        collapseId = notificationObject[COLLAPSE_ID].getStringOrDefault(uuidProvider.provide()),
                        operation = NotificationOperation.valueOf(
                            notificationObject[OPERATION].getStringOrDefault("INIT").uppercase()
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
                actionableData = actionableData
            )
        } catch (exception: Exception) {
            logger.error("push mapping failed", exception)
            null
        }
    }
}