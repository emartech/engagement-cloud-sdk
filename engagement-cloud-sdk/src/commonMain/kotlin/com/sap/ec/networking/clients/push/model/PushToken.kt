package com.sap.ec.networking.clients.push.model

import kotlinx.serialization.Serializable

@Serializable
internal data class PushToken(val pushToken: String)
