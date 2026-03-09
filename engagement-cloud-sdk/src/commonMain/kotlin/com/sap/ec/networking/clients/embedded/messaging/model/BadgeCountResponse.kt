package com.sap.ec.networking.clients.embedded.messaging.model

import kotlinx.serialization.Serializable

@Serializable
internal data class BadgeCountResponse(val version: String, val count: Int)
