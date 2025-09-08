package com.emarsys.mobileengage.embedded.message

import kotlinx.serialization.Serializable

@Serializable
data class MessageTagUpdate(
    val messageId: String,
    val operation: TagOperation,
    val tag: String,
    val trackingInfo: String
)

@Serializable
enum class TagOperation {
    Add, Remove
}