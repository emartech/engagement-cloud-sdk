package com.emarsys.api.push

import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import kotlinx.serialization.Serializable

@Serializable
data class PushUserInfo(
    val ems: Ems,
    val notification: Notification
)

@Serializable
data class Notification(
    val silent: Boolean = false,
    val defaultAction: BasicActionModel? = null,
    val actions: List<PresentableActionModel>? = null,
    val badgeCount: BadgeCount? = null
)

@Serializable
data class Ems(
    val version: String,
    val trackingInfo: String
)
