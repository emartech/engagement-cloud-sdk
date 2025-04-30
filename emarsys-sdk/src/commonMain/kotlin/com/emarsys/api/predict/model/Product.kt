package com.emarsys.api.predict.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@Serializable
@OptIn(ExperimentalJsExport::class)
@JsExport
data class Product(
    val productId: String,
    val title: String,
    val linkUrl: String,
    val feature: String,
    val cohort: String,
    val customFields: Map<String, String?> = mutableMapOf(),
    private val imageUrlString: String? = null,
    private val zoomImageUrlString: String? = null,
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
) {
    val imageUrl: Url?  get() = if (imageUrlString != null) Url(imageUrlString) else null

    val zoomImageUrl: Url? get() = if (zoomImageUrlString != null) Url(zoomImageUrlString) else null
}