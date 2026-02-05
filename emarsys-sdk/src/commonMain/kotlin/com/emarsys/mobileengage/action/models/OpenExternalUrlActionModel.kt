package com.emarsys.mobileengage.action.models

import com.emarsys.core.providers.UUIDProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface OpenExternalUrlActionModel {
    val url: String
    val target: HtmlTarget?
}

@Serializable
@SerialName("OpenExternalUrl")
data class PresentableOpenExternalUrlActionModel(
    override val id: String = UUIDProvider().provide(),
    override val reporting: String,
    override val title: String,
    override val url: String,
    override val target: HtmlTarget? = null
) : PresentableActionModel(), OpenExternalUrlActionModel


@Serializable
@SerialName("OpenExternalUrl")
data class BasicOpenExternalUrlActionModel(
    override val reporting: String = "",
    override val url: String,
    override val target: HtmlTarget? = null
) : BasicActionModel(), OpenExternalUrlActionModel

@Serializable
enum class HtmlTarget(val raw: String) {
    @SerialName("_blank")
    BLANK("_blank"),
    @SerialName("_top")
    TOP("_top"),
    @SerialName("_parent")
    PARENT("_parent"),
    @SerialName("_self")
    SELF("_self")
}