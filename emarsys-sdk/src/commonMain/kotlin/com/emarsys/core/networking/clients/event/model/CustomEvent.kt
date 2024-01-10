package com.emarsys.core.networking.clients.event.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomEvent(
    val eventName: String
)