package com.sap.ec.mobileengage.action.models

import com.sap.ec.core.providers.UUIDProvider
import com.sap.ec.mobileengage.inapp.networking.models.PushToInAppPayload
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface PushToInAppActionModel {
    val payload: PushToInAppPayload
}

@Serializable
@SerialName("InApp")
data class BasicPushToInAppActionModel(
    override val reporting: String,
    override val payload: PushToInAppPayload
) : BasicActionModel(), PushToInAppActionModel

@Serializable
@SerialName("InApp")
data class PresentablePushToInAppActionModel(
    override val id: String = UUIDProvider().provide(),
    override val reporting: String,
    override val title: String,
    override val payload: PushToInAppPayload
) : PresentableActionModel(), PushToInAppActionModel
