package com.emarsys.mobileengage.push.model.v1

import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RemoteWebPushMessageV2(
    val notification: RemoteWebPushNotificationV2,
    val ems: EmsPushData
)

@Serializable
data class RemoteWebPushNotificationV2(
    val silent: Boolean,
    val title: String,
    val body: String,
    val icon: String,
    val imageUrl: String,
    val defaultAction: BasicActionModel? = null,
    val actions: List<PresentableActionModel>? = null,
    val badgeCount: BadgeCount? = null
)

@Serializable
data class EmsPushData(
    val version: String,
    val trackingInfo: String,
    val rootParams: JsonObject? = null,
    val customData: JsonObject? = null
)
