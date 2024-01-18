package com.emarsys.networking.clients.event.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomEvent(
    val eventName: String
)