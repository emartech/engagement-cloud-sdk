package com.sap.ec.api.push

import com.sap.ec.mobileengage.action.models.BadgeCount
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import kotlinx.serialization.Serializable

@Serializable
data class PushUserInfo(
    val ems: Ems,
    val notification: Notification
)

@Serializable
data class SilentPushUserInfo(
    val ems: Ems,
    val notification: SilentNotification
)

@Serializable
data class Notification(
    val silent: Boolean = false,
    val defaultAction: BasicActionModel? = null,
    val actions: List<PresentableActionModel>? = null,
    val badgeCount: BadgeCount? = null
)

@Serializable
data class SilentNotification(
    val silent: Boolean = false,
    val defaultAction: BasicActionModel? = null,
    val actions: List<BasicActionModel>? = null,
    val badgeCount: BadgeCount? = null
)

@Serializable
data class Ems(
    val version: String,
    val trackingInfo: String
)
