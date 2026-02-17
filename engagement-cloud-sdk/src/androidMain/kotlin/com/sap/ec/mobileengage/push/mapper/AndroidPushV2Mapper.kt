package com.sap.ec.mobileengage.push.mapper

import com.sap.ec.core.log.Logger
import com.sap.ec.core.mapper.Mapper
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.mobileengage.push.ActionableData
import com.sap.ec.mobileengage.push.DisplayableData
import com.sap.ec.mobileengage.push.NotificationOperation
import com.sap.ec.mobileengage.push.model.AndroidPlatformData
import com.sap.ec.mobileengage.push.model.AndroidPushMessage
import com.sap.ec.mobileengage.push.model.NotificationMethod
import com.sap.ec.mobileengage.push.model.NotificationStyle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

internal class AndroidPushV2Mapper(
    private val uuidProvider: UuidProviderApi,
    private val logger: Logger,
    private val json: Json
) : Mapper<JsonObject, AndroidPushMessage> {

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
        private const val BADGE_COUNT = "notification.badgeCount"
        private const val TRACKING_INFO = "ems.trackingInfo"
    }

    override suspend fun map(from: JsonObject): AndroidPushMessage? {
        return try {
            val trackingInfo: String = from[TRACKING_INFO]?.jsonPrimitive?.content ?: "{}"
            val defaultTapAction: BasicActionModel? =
                from[DEFAULT_ACTION]?.jsonPrimitive?.contentOrNull.fromString(json)
            val actions: List<PresentableActionModel>? =
                from[ACTIONS]?.jsonPrimitive?.contentOrNull.fromString(json)

            val actionableData =
                ActionableData(actions = actions, defaultTapAction = defaultTapAction)

            AndroidPushMessage(
                trackingInfo = trackingInfo,
                platformData = AndroidPlatformData(
                    channelId = from[CHANNEL_ID]?.jsonPrimitive?.contentOrNull
                        ?: DEFAULT_CHANNEL_ID,
                    notificationMethod = NotificationMethod(
                        collapseId = from[COLLAPSE_ID]?.jsonPrimitive?.contentOrNull
                            ?: uuidProvider.provide(),
                        operation = from[OPERATION]?.jsonPrimitive?.contentOrNull?.let {
                            NotificationOperation.valueOf(
                                it.uppercase()
                            )
                        } ?: NotificationOperation.INIT
                    ),
                    style = from[STYLE]?.jsonPrimitive?.contentOrNull?.let {
                        NotificationStyle.valueOf(
                            it
                        )
                    }
                ),
                badgeCount = from[BADGE_COUNT]?.jsonPrimitive?.contentOrNull.fromString(json),
                displayableData = DisplayableData(
                    from.getValue(TITLE).jsonPrimitive.content,
                    from.getValue(BODY).jsonPrimitive.content,
                    iconUrlString = from[ICON]?.jsonPrimitive?.contentOrNull,
                    imageUrlString = from[IMAGE_URL]?.jsonPrimitive?.contentOrNull
                ),
                actionableData = actionableData
            )
        } catch (e: Exception) {
            logger.error("push mapping failed", e)
            null
        }
    }
}

inline fun <reified T> String?.fromString(json: Json): T? {
    return this?.let { json.decodeFromString<T>(it) }
}