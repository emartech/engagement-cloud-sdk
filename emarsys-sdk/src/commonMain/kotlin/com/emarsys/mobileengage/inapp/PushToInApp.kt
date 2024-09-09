package com.emarsys.mobileengage.inapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushToInApp(
    @SerialName("campaign_id")
    val campaignId: String,
    val url: String,
    val ignoreViewedEvent: Boolean? = null
)