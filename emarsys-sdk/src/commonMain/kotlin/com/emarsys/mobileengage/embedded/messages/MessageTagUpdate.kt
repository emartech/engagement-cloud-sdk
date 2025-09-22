package com.emarsys.mobileengage.embedded.messages

import kotlinx.serialization.SerialName
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
    @SerialName("add") Add,
    @SerialName("remove") Remove
}