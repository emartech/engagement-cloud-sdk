package com.emarsys.api.predict.model

import io.ktor.http.*


data class Product(
    val productId: String,
    val title: String,
    val linkUrl: String,
    val feature: String,
    val cohort: String,
    val customFields: Map<String, String?> = mutableMapOf(),
    private val imageUrlString: String? = null,
    val imageUrl: Url? = if (imageUrlString != null) Url(imageUrlString) else null,
    private val zoomImageUrlString: String? = null,
    val zoomImageUrl: Url? = if (zoomImageUrlString != null) Url(zoomImageUrlString) else null,
    val categoryPath: String? = null,
    val available: Boolean? = null,
    val productDescription: String? = null,
    val price: Float? = null,
    val msrp: Float? = null,
    val album: String? = null,
    val actor: String? = null,
    val artist: String? = null,
    val author: String? = null,
    val brand: String? = null,
    val year: Int? = null
)