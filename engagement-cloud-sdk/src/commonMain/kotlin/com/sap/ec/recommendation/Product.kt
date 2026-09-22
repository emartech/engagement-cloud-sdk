package com.sap.ec.recommendation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @SerialName("item")
    val productId: String,
    val title: String,
    @SerialName("link")
    val linkUrl: String,
    val customFields: Map<String, String?> = emptyMap(),
    @SerialName("image")
    val imageUrl: String? = null,
    @SerialName("zoom_image")
    val zoomImageUrl: String? = null,
    @SerialName("category")
    val categoryPath: String? = null,
    val available: Boolean? = null,
    val description: String? = null,
    val price: Double? = null,
    val msrp: Double? = null,
    val brand: String? = null,
    val feature: String? = null,
    val cohort: String? = null,
)