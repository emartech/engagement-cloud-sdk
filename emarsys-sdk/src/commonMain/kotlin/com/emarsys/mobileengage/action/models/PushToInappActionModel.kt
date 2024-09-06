package com.emarsys.mobileengage.action.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PushToInappCampaign")
data object PushToInappActionModel : BasicActionModel()

@Serializable
data class InternalPushToInappActionModel(
    val campaignId: String,
    val url: String,
    val html: String? = null,
    val ignoreViewedEvent: Boolean? = null
) : BasicActionModel() {
}