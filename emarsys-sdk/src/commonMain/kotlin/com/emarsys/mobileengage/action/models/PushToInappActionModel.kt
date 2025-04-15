package com.emarsys.mobileengage.action.models

import com.emarsys.core.providers.UUIDProvider
import com.emarsys.mobileengage.inapp.PushToInApp
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
) : BasicActionModel()

@Serializable
@SerialName("MEInApp")
data class PresentablePushToInAppActionModel(
    override val id: String = UUIDProvider().provide(),
    override val title: String,
    override val reporting: String,
    val name: String,
    val payload: PushToInApp
) : PresentableActionModel()

@Serializable
@SerialName("MEInApp")
data class BasicPushToInAppActionModel(
    val name: String,
    val payload: PushToInApp
) : BasicActionModel()

fun BasicPushToInAppActionModel.toInternalPushToInAppActionModel(): InternalPushToInappActionModel {
    return InternalPushToInappActionModel(payload.campaignId, payload.url)
}
