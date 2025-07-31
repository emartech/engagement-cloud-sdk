package com.emarsys.mobileengage.inapp

import kotlinx.serialization.Serializable

@Serializable
data class PushToInAppPayload(
    val url: String,
    val ignoreViewedEvent: Boolean? = null
)