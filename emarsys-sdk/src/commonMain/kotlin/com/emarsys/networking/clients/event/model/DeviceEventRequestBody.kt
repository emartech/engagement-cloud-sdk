package com.emarsys.networking.clients.event.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceEventRequestBody(
    val dnd: Boolean = false,
    val events: List<DeviceEvent>,
    val deviceEventState: String? = null
)