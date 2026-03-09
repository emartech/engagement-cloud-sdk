package com.sap.ec.networking.clients.embedded.messaging.model

import kotlinx.serialization.Serializable

@Serializable
internal data class TagsResponse(
    val version: String,
    val totalChanges: Int,
    val changes: List<TagChange>
)

@Serializable
internal data class TagChange(
    val messageId: String,
    val operation: OperationType,
    val tag: String,
    val trackingInfo: String
)

internal enum class OperationType {
    ADD,
    REMOVE
}
