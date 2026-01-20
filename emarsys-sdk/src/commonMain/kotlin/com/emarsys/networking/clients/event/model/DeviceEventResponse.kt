package com.emarsys.networking.clients.event.model

import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.InAppType
import kotlinx.serialization.Serializable

@Serializable
data class DeviceEventResponse(
    val contentCampaigns: List<ContentCampaign>? = null,
    val actionCampaigns: List<OnEventActionCampaign>? = null,
    val deviceEventState: String? = null
)

@Serializable
data class OnEventActionCampaign(
    val trackingInfo: String,
    val actions: List<BasicActionModel>
)

@Serializable
data class ContentCampaign(
    val type: InAppType,
    val trackingInfo: String,
    val content: String
)

fun ContentCampaign.asInAppMessage(): InAppMessage {
    return InAppMessage(type = this.type, trackingInfo = this.trackingInfo, content = this.content)
}