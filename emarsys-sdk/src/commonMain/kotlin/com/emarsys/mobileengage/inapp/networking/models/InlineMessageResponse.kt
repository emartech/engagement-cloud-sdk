package com.emarsys.mobileengage.inapp.networking.models

import com.emarsys.mobileengage.inapp.InlineMessageItem
import kotlinx.serialization.Serializable

@Serializable
internal data class InlineMessageResponse(
    val inlineMessages: List<InlineMessageItem>? = null
)
