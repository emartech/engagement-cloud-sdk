package com.sap.ec.mobileengage.action.models

import com.sap.ec.core.providers.UUIDProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface AppEventActionModel {
    val name: String
    val payload: Map<String, String>?
}

@Serializable
@SerialName("MEAppEvent")
data class PresentableAppEventActionModel(
    override val id: String = UUIDProvider().provide(),
    override val reporting: String,
    override val title: String,
    override val name: String,
    override val payload: Map<String, String>?,
) : PresentableActionModel(), AppEventActionModel

@Serializable
@SerialName("MEAppEvent")
data class BasicAppEventActionModel(
    override val reporting: String = "",
    override val name: String,
    override val payload: Map<String, String>?
) : BasicActionModel(), AppEventActionModel