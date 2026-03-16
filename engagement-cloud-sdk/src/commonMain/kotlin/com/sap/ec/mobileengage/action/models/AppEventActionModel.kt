package com.sap.ec.mobileengage.action.models

import com.sap.ec.api.event.model.EventSource
import com.sap.ec.core.providers.UUIDProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal interface AppEventActionModel {
    val name: String
    val payload: Map<String, String>?
    val source: EventSource?
}

@Serializable
@SerialName("MEAppEvent")
internal data class PresentableAppEventActionModel(
    override val id: String = UUIDProvider().provide(),
    override val reporting: String,
    override val title: String,
    override val name: String,
    override val payload: Map<String, String>?,
    override val source: EventSource? = null,
) : PresentableActionModel(), AppEventActionModel

@Serializable
@SerialName("MEAppEvent")
internal data class BasicAppEventActionModel(
    override val reporting: String = "",
    override val name: String,
    override val payload: Map<String, String>?,
    override val source: EventSource? = null,
) : BasicActionModel(), AppEventActionModel