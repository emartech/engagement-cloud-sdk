package com.sap.ec.networking.clients.embedded.messaging.model

import kotlinx.serialization.Serializable

@Serializable
data class BadgeCountResponse(val version: String, val count: Int)
