package com.sap.ec.mobileengage.push.model.v1

import com.sap.ec.mobileengage.action.models.BadgeCount
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class RemoteWebPushMessageV2(
    val notification: RemoteWebPushNotificationV2,
    val ems: EmsPushData
)

@Serializable
internal data class RemoteWebPushNotificationV2(
    val silent: Boolean,
    val title: String,
    val body: String,
    val icon: String? = null,
    val imageUrl: String? = null,
    val defaultAction: BasicActionModel? = null,
    val actions: List<PresentableActionModel>? = null,
    val badgeCount: BadgeCount? = null
)

@Serializable
internal data class EmsPushData(
    val version: String,
    val trackingInfo: String,
    val rootParams: JsonObject? = null,
    val customData: JsonObject? = null
)
