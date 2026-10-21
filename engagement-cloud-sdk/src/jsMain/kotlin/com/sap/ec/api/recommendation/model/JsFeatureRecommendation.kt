package com.sap.ec.api.recommendation.model

import com.sap.ec.recommendation.models.FeatureRecommendation

@OptIn(ExperimentalJsExport::class)
@JsName("FeatureRecommendation")
@JsExport
interface JsFeatureRecommendation {
    val featureName: String
    val topicLabel: String?
    val hasMore: Boolean
    val merchants: Array<String>
    val products: Array<JsProduct>
}

@OptIn(ExperimentalWasmJsInterop::class)
fun FeatureRecommendation.toJsFeatureRecommendation(): JsFeatureRecommendation {
    return object : JsFeatureRecommendation {
        override val featureName = this@toJsFeatureRecommendation.featureName
        override val topicLabel= this@toJsFeatureRecommendation.topicLabel
        override val hasMore = this@toJsFeatureRecommendation.hasMore
        override val merchants = this@toJsFeatureRecommendation.merchants.toJsArray()
        override val products = this@toJsFeatureRecommendation.products.toJSProducts()
    }
}
