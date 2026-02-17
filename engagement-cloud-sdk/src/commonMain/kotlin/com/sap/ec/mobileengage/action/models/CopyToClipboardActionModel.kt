package com.sap.ec.mobileengage.action.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface CopyToClipboardActionModel {
    val text: String
}

@Serializable
@SerialName("copyToClipboard")
data class BasicCopyToClipboardActionModel(
    override val reporting: String = "",
    override val text: String
) : BasicActionModel(), CopyToClipboardActionModel