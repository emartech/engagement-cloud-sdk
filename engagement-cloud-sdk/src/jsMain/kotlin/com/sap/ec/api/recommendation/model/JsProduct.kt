package com.sap.ec.api.recommendation.model

import com.sap.ec.recommendation.models.product.Product

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsProduct {
    val productId: String
    val title: String
    val linkUrl: String
    val customFields: Map<String, String?>
    val imageUrl: String?
    val zoomImageUrl: String?
    val categoryPath: String?
    val available: Boolean?
    val description: String?
    val price: Double?
    val msrp: Double?
    val brand: String?
    val feature: String?
    val cohort: String?
}

@OptIn(ExperimentalWasmJsInterop::class)
fun List<Product>.toJSProducts(): Array<JsProduct> {
    return this.map {
        object : JsProduct {
            override val productId = it.productId
            override val title = it.title
            override val linkUrl = it.linkUrl
            override val customFields = it.customFields
            override val imageUrl = it.imageUrl
            override val zoomImageUrl = it.zoomImageUrl
            override val categoryPath = it.categoryPath
            override val available = it.available
            override val description = it.description
            override val price = it.price
            override val msrp = it.msrp
            override val brand = it.brand
            override val feature = it.feature
            override val cohort = it.cohort
        }
    }.toJsArray()
}