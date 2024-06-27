package com.emarsys.mobileengage.action.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface DismissActionModel {
    var topic: String?
}

@Serializable
@SerialName("Dismiss")
data class PresentableDismissActionModel(
    override val id: String,
    override val title: String,
    override var topic: String? = null
) : PresentableActionModel(), DismissActionModel

@Serializable
@SerialName("Dismiss")
data class BasicDismissActionModel(
    override var topic: String? = null
) : BasicActionModel(), DismissActionModel
