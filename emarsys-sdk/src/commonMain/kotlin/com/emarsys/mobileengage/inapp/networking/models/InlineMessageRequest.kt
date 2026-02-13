package com.emarsys.mobileengage.inapp.networking.models

import kotlinx.serialization.Serializable

@Serializable
internal data class InlineMessageRequest(
    val viewIds: List<String>
)
