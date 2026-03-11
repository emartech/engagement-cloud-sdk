package com.sap.ec.mobileengage.action.models

import com.sap.ec.core.providers.UUIDProvider
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal interface DismissActionModel {
    var dismissId: String?
    var inAppType: InAppType?
}

@Serializable
@SerialName("Dismiss")
internal data class PresentableDismissActionModel(
    override val id: String = UUIDProvider().provide(),
    override val reporting: String,
    override val title: String,
    override var dismissId: String? = null,
    override var inAppType: InAppType? = null
) : PresentableActionModel(), DismissActionModel

@Serializable
@SerialName("Dismiss")
internal data class BasicDismissActionModel(
    override val reporting: String = "",
    override var dismissId: String? = null,
    override var inAppType: InAppType? = null
) : BasicActionModel(), DismissActionModel
