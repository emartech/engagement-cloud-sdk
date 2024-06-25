package com.emarsys.mobileengage.action.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface CustomEventActionModel {
    val name: String
    val payload: Map<String, String>?
}

@Serializable
@SerialName("MECustomEvent")
data class PresentableCustomEventActionModel(
    override val id: String,
    override val title: String,
    override val name: String,
    override val payload: Map<String, String>?,
):  PresentableActionModel(), CustomEventActionModel

@Serializable
@SerialName("MECustomEvent")
data class DefaultCustomEventActionModel(
    override val name: String,
    override val payload: Map<String, String>?,
): DefaultActionModel(), CustomEventActionModel