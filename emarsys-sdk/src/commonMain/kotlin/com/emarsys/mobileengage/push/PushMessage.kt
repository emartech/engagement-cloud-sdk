package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.models.ActionModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class PushMessage(
    val messageId: String,
    val title: String,
    val body: String,
    val iconUrlString: String? = null,
    val imageUrlString: String? = null,
    val data: PushData? = null
)

@Serializable
data class PushData(
    val silent: Boolean = false,
    val sid: String,
    val campaignId: String,
    val platformContext: JsonObject? = null,
    val defaultAction: ActionModel? = null,
    val actions: List<ActionModel>? = null,
    val inApp: InApp? = null,
    val rootParams: JsonObject? = null,
    val u: JsonObject? = null
)

@Serializable
data class InApp(
    val campaignId: String,
    val urlString: String,
    val ignoreViewedEvent: Boolean? = null
)
