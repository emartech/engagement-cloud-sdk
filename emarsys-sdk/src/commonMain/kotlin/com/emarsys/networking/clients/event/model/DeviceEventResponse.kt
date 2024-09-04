package com.emarsys.networking.clients.event.model

import com.emarsys.mobileengage.action.models.BasicActionModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DeviceEventResponse(
    val message: EventResponseInApp? = null,
    val onEventAction: OnEventAction? = null,
    val deviceEventState: JsonObject? = null
)

@Serializable
data class OnEventAction(
    val campaignId: String,
    val actions: List<BasicActionModel>
)

@Serializable
data class EventResponseInApp(
    val campaignId: String,
    val html: String
)