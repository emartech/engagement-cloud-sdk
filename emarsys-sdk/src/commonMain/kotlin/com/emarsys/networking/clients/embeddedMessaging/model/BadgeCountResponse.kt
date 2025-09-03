package com.emarsys.networking.clients.embeddedMessaging.model

import kotlinx.serialization.Serializable

@Serializable
data class BadgeCountResponse(val version: String, val count: Int)
