package com.sap.ec.recommendation

import kotlinx.serialization.Serializable

@Serializable
data class Feature(
    val topicLabel: String?,
    val hasMore: Boolean,
    val merchants: List<String>,
    val items: List<Item>
)

@Serializable
data class Item(
    val id: String
)