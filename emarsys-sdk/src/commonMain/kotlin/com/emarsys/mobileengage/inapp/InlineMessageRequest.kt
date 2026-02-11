package com.emarsys.mobileengage.inapp

import kotlinx.serialization.Serializable

@Serializable
internal data class InlineMessageRequest(
    val viewIds: List<String>
)
