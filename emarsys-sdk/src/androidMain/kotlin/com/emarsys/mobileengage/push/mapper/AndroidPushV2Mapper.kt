package com.emarsys.mobileengage.push.mapper

import com.emarsys.core.log.Logger
import com.emarsys.core.mapper.Mapper
import com.emarsys.core.providers.UuidProviderApi
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
        private const val TREATMENTS = "ems.treatments"
        private const val ACTIONS = "notification.actions"
        private const val IN_APP = "notification.inapp"
        private const val BADGE_COUNT = "notification.badgeCount"
        private const val SID_KEY = "sid"
        private const val CAMPAIGN_ID_KEY = "campaignId"
        private const val MISSING_CAMPAIGN_ID = "missingCampaignId"
        private const val MISSING_SID = "missingSID"
    }

    override suspend fun map(from: JsonObject): AndroidPushMessage? {
        return try {
            val treatments: JsonObject? =
                from[TREATMENTS]?.jsonPrimitive?.contentOrNull.fromString(json)
            val defaultTapAction: BasicActionModel? =
                from[DEFAULT_ACTION]?.jsonPrimitive?.contentOrNull.fromString(json)
            val actions: List<PresentableActionModel>? =
                from[ACTIONS]?.jsonPrimitive?.contentOrNull.fromString(json)
            val pushToInApp: PushToInApp? =
                from[IN_APP]?.jsonPrimitive?.contentOrNull.fromString(json)

            val actionableData =
                ActionableData(
                    actions = actions,
                    defaultTapAction = defaultTapAction,
                    pushToInApp = pushToInApp
                )

            AndroidPushMessage(
                sid = extractStringWithFallback(treatments, SID_KEY, MISSING_SID),
                campaignId = extractStringWithFallback(
                    treatments,
                    CAMPAIGN_ID_KEY,
                    MISSING_CAMPAIGN_ID
                ),
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

    private suspend fun extractStringWithFallback(
        from: JsonObject?,
        key: String,
        fallback: String
    ): String {
        val result = from?.get(key)?.jsonPrimitive?.contentOrNull
        return if (result != null) {
            result
        } else {
            logger.error(
                "Failed to extract $key from push message",
                Exception("Missing key: $key")
            )
            fallback
        }
    }
}

inline fun <reified T> String?.fromString(json: Json): T? {
    return this?.let { json.decodeFromString<T>(it) }
}