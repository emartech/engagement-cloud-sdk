package com.emarsys.mobileengage.inapp

import kotlinx.serialization.Serializable

@Serializable
internal data class InlineMessageItem(
    val type: String,
    val trackingInfo: String,
    val content: String,
    val viewId: String
)
