package com.emarsys.core.networking.clients.event.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceEventRequestBody(
    val dnd: Boolean = false,
    val events: List<Event>,
    val deviceEventState: String? = null
)