package com.emarsys.mobileengage.inapp

import kotlinx.serialization.Serializable

@Serializable
data class PushToInApp(
    val campaignId: String,
    val url: String,
    val ignoreViewedEvent: Boolean? = null
)