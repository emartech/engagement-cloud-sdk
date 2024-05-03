package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.models.ActionModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class PushMessage(
    val title: String,
    val body: String,
    val iconUrlString: String?,
    val imageUrlString: String?,
    val data: PushData?
)

@Serializable
data class PushData(
    val silent: Boolean,
    val sid: String,
    val platformContext: JsonObject?,
    val defaultAction: ActionModel?,
    val actions: List<ActionModel>?,
    val inApp: InApp?,
    val rootParams: JsonObject?
)

@Serializable
data class InApp(
    val campaignId: String,
    val urlString: String,
    val ignoreViewedEvent: Boolean?
)
