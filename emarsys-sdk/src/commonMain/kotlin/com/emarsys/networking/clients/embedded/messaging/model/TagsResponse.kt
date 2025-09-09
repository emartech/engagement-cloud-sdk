package com.emarsys.networking.clients.embedded.messaging.model

import kotlinx.serialization.Serializable

@Serializable
data class TagsResponse(
    val version: String,
    val totalChanges: Int,
    val changes: List<TagChange>
)

@Serializable
data class TagChange(
    val messageId: String,
    val operation: OperationType,
    val tag: String,
    val trackingInfo: String
)

enum class OperationType {
    ADD,
    REMOVE
}
