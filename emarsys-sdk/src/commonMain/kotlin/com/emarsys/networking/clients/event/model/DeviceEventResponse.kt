package com.emarsys.networking.clients.event.model

import com.emarsys.action.GenericAction
import kotlinx.serialization.Serializable

@Serializable
data class DeviceEventResponse(
    val message: Map<String, String?>? = null,
    val onEventAction: OnEventAction? = null,
    val deviceEventState: String?
)

@Serializable
data class OnEventAction(
    val campaignId: String,
    val actions: List<GenericAction>
)