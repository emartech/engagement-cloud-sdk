package com.emarsys.mobileengage.inapp

import kotlinx.serialization.Serializable

@Serializable
internal data class InlineMessageResponse(
    val inlineMessages: List<InlineMessageItem>? = null
)
