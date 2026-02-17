package com.sap.ec.mobileengage.inapp.networking.models

import com.sap.ec.mobileengage.inapp.InlineMessageItem
import kotlinx.serialization.Serializable

@Serializable
internal data class InlineMessageResponse(
    val inlineMessages: List<InlineMessageItem>? = null
)
