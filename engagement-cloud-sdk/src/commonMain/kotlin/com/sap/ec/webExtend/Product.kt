package com.sap.ec.webExtend

data class Product(
    val productId: String,
    val title: String,
    val linkUrl: String,
    val customFields: Map<String, String?> = emptyMap(),
    val imageUrl: String? = null,
    val zoomImageUrl: String? = null,
    val categoryPath: String? = null,
    val available: Boolean? = null,
    val productDescription: String? = null,
    val price: Double? = null,
    val msrp: Double? = null,
    val brand: String? = null,
    val feature: String? = null,
    val cohort: String? = null,
)
