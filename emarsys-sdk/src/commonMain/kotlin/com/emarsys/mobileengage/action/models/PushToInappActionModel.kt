package com.emarsys.mobileengage.action.models

import com.emarsys.core.providers.UUIDProvider
import com.emarsys.mobileengage.inapp.PushToInAppPayload
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface InAppActionModel {
    var trackingInfo: String?
}

@Serializable
@SerialName("InApp")
data class BasicPushToInAppActionModel(
    val id: String = UUIDProvider().provide(),
    override val reporting: String,
    val payload: PushToInAppPayload,
    override var trackingInfo: String? = null
) : BasicActionModel(), InAppActionModel

@Serializable
@SerialName("InApp")
data class PresentablePushToInAppActionModel(
    override val id: String = UUIDProvider().provide(),
    override val reporting: String,
    override val title: String,
    val payload: PushToInAppPayload,
    override var trackingInfo: String? = null
) : PresentableActionModel(), InAppActionModel


fun BasicPushToInAppActionModel.toPresentablePushToInAppActionModel(): PresentablePushToInAppActionModel {
    return PresentablePushToInAppActionModel(id, reporting, title = "", payload, trackingInfo)
}
