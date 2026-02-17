package com.sap.ec.mobileengage.action.models

import com.sap.ec.core.providers.UUIDProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface DismissActionModel {
    var dismissId: String?
}

@Serializable
@SerialName("Dismiss")
data class PresentableDismissActionModel(
    override val id: String = UUIDProvider().provide(),
    override val reporting: String,
    override val title: String,
    override var dismissId: String? = null
) : PresentableActionModel(), DismissActionModel

@Serializable
@SerialName("Dismiss")
data class BasicDismissActionModel(
    override val reporting: String = "",
    override var dismissId: String? = null
) : BasicActionModel(), DismissActionModel
