package com.sap.ec.mobileengage.action.models

import com.sap.ec.core.providers.UUIDProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface CustomEventActionModel {
    val name: String
    val payload: Map<String, String>?
}

@Serializable
@SerialName("MECustomEvent")
data class PresentableCustomEventActionModel(
    override val id: String = UUIDProvider().provide(),
    override val reporting: String,
    override val title: String,
    override val name: String,
    override val payload: Map<String, String>? = null,
) : PresentableActionModel(), CustomEventActionModel

@Serializable
@SerialName("MECustomEvent")
data class BasicCustomEventActionModel(
    override val reporting: String = "",
    override val name: String,
    override val payload: Map<String, String>? = null,
) : BasicActionModel(), CustomEventActionModel