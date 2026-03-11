package com.sap.ec.mobileengage.action.models

import com.sap.ec.mobileengage.inapp.presentation.InAppType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal interface CopyToClipboardActionModel {
    val text: String
}

@Serializable
@SerialName("copyToClipboard")
internal data class BasicCopyToClipboardActionModel(
    override val reporting: String = "",
    override val text: String,
) : BasicActionModel(), CopyToClipboardActionModel