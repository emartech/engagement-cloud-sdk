package com.sap.ec.api.recommendation.model

import com.sap.ec.recommendation.models.RecommendationResult

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsRecommendationResult {
    val featureName: String
    val topicLabel: String?
    val hasMore: Boolean
    val merchants: Array<String>
    val products: Array<JsProduct>
}

@OptIn(ExperimentalWasmJsInterop::class)
fun RecommendationResult.toJSRecommendationResult(): JsRecommendationResult {
    return object : JsRecommendationResult {
        override val featureName = this@toJSRecommendationResult.featureName
        override val topicLabel= this@toJSRecommendationResult.topicLabel
        override val hasMore = this@toJSRecommendationResult.hasMore
        override val merchants = this@toJSRecommendationResult.merchants.toJsArray()
        override val products = this@toJSRecommendationResult.products.toJSProducts()
    }
}