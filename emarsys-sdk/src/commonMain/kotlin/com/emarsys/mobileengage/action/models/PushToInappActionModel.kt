package com.emarsys.mobileengage.action.models

import com.emarsys.core.providers.UUIDProvider
import com.emarsys.mobileengage.inapp.PushToInAppPayload
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("InApp")
data class BasicPushToInAppActionModel(
    val id: String = UUIDProvider().provide(),
    override val reporting: String,
    val payload: PushToInAppPayload
) : BasicActionModel()

@Serializable
@SerialName("InApp")
data class PresentablePushToInAppActionModel(
    override val id: String = UUIDProvider().provide(),
    override val reporting: String,
    override val title: String,
    val payload: PushToInAppPayload
) : PresentableActionModel()


fun BasicPushToInAppActionModel.toPresentablePushToInAppActionModel(): PresentablePushToInAppActionModel {
    return PresentablePushToInAppActionModel(id, reporting, title = "", payload)
}
