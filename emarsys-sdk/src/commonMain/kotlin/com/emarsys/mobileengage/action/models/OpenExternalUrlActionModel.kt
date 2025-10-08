package com.emarsys.mobileengage.action.models

import com.emarsys.core.providers.UUIDProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface OpenExternalUrlActionModel {
    val url: String
}

@Serializable
@SerialName("OpenExternalUrl")
data class PresentableOpenExternalUrlActionModel(
    override val id: String = UUIDProvider().provide(),
    override val reporting: String,
    override val title: String,
    override val url: String
) : PresentableActionModel(), OpenExternalUrlActionModel


@Serializable
@SerialName("OpenExternalUrl")
data class BasicOpenExternalUrlActionModel(
    override val reporting: String = "",
    override val url: String
) : BasicActionModel(), OpenExternalUrlActionModel
