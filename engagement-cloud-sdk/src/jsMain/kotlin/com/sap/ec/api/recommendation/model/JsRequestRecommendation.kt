@file:OptIn(ExperimentalJsExport::class)

package com.sap.ec.api.recommendation.model


@JsExport
interface JsRecommendationOptions {
    val logic: String
    val variants: Array<String>?
    val limit: Int
    val offset: Int
    val filters: Array<JsRecommendationFilter>?
    val availabilityZone: String?
    val language: String?
    val displayCurrency: String?
}

@JsExport
enum class JsRecommendationLogic {
    RELATED,
    ALSOBOUGHT,
    CART,
    CATEGORY,
    POPULAR,
    SEARCH,
    PERSONAL,
    HOME
}

@JsExport
interface JsRecommendationFilter {
    val type: String
    val field: String
    val comparison: String
    val expectations: Array<String>
}

@JsExport
enum class JsFilterType {
    INCLUDE,
    EXCLUDE
}

@JsExport
enum class JsComparisonType {
    IS,
    IN,
    HAS,
    OVERLAPS
}