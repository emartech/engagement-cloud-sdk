package com.emarsys.mobileengage.push.model.v1

import com.emarsys.mobileengage.action.models.PresentableActionModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RemoteWebPushMessageV1(
    val notification: RemoteWebPushNotificationV1,
    val ems: EmsPushData
)

@Serializable
data class RemoteWebPushNotificationV1(
    val silent: Boolean,
    val title: String,
    val body: String,
    val icon: String,
    val imageUrl: String,
    val defaultAction: PresentableActionModel?,
    val actions: List<PresentableActionModel>?,
)

@Serializable
data class EmsPushData(
    val version: String,
    val id: String,
    val applicationCode: String,
    val campaignId: String,
    val productId: String,
    val multiChannelId: String,
    val sid: String,
    val treatments: JsonObject,
    val rootParams: JsonObject?
)
