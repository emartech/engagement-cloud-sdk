@file:OptIn(ExperimentalJsExport::class)

package com.sap.ec.api.recommendation.model


@JsExport
@JsName("RecommendationOptions")
interface JsRecommendationOptions {
    val logic: String
    val variants: Array<String>?
    val limit: Int?
    val offset: Int?
    val filters: Array<JsRecommendationFilter>?
    val availabilityZone: String?
    val language: String?
    val displayCurrency: String?
}

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
@JsName("RecommendationFilter")
interface JsRecommendationFilter {
    val type: String
    val field: String
    val comparison: String
    val expectations: Array<String>
}

enum class JsFilterType {
    INCLUDE,
    EXCLUDE
}

enum class JsComparisonType {
    IS,
    IN,
    HAS,
    OVERLAPS
}