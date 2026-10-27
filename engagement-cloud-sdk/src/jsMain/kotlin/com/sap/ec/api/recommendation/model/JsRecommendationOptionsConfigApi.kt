@file:OptIn(ExperimentalJsExport::class)

package com.sap.ec.api.recommendation.model

@JsExport
@JsName("RecommendationOptionsConfig")
interface JsRecommendationOptionsConfigApi {
    fun withVariants(variants: Array<String>): JsRecommendationOptionsConfigApi
    fun withLimit(limit: Int): JsRecommendationOptionsConfigApi
    fun withOffset(offset: Int): JsRecommendationOptionsConfigApi
    fun withFilters(filters: Array<JsRecommendationFilter>): JsRecommendationOptionsConfigApi
    fun withAvailabilityZone(availabilityZone: String): JsRecommendationOptionsConfigApi
    fun withLanguage(language: String): JsRecommendationOptionsConfigApi
    fun withDisplayCurrency(displayCurrency: String): JsRecommendationOptionsConfigApi
    fun build(): JsRecommendationOptions
}
