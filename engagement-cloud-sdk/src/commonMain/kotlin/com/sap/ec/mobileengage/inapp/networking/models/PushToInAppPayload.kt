package com.sap.ec.mobileengage.inapp.networking.models

import kotlinx.serialization.Serializable

@Serializable
internal data class PushToInAppPayload(
    val url: String,
    val ignoreViewedEvent: Boolean? = null
)