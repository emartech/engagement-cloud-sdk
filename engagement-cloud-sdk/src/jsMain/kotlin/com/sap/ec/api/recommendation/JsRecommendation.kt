package com.sap.ec.api.recommendation

import com.sap.ec.api.recommendation.model.JsComparisonType
import com.sap.ec.api.recommendation.model.JsFilterType
import com.sap.ec.api.recommendation.model.JsRecommendationFilter
import com.sap.ec.api.recommendation.model.JsRecommendationLogic
import com.sap.ec.api.recommendation.model.JsRecommendationOptions
import com.sap.ec.api.recommendation.model.JsRecommendationResult
import com.sap.ec.api.recommendation.model.toJsRecommendationResult
import com.sap.ec.api.tracking.model.JsRecommendationEvent
import com.sap.ec.api.tracking.util.parseRecommendationEvent
import com.sap.ec.recommendation.models.requestRecommendation.ComparisonType
import com.sap.ec.recommendation.models.requestRecommendation.FilterType
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationFilter
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationLogic
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions

internal class JsRecommendation(
    private val recommendation: RecommendationApi
) : JsRecommendationApi {
    override suspend fun requestRecommendation(
        recommendationOptions: JsRecommendationOptions,
        contextEvent: JsRecommendationEvent?
    ): JsRecommendationResult {
        return recommendation.requestRecommendation(
            recommendationOptions = recommendationOptions.toRecommendationOptions(),
            contextEvent = contextEvent?.let { parseRecommendationEvent(it) }
        ).getOrThrow().toJsRecommendationResult()
    }

    private fun JsRecommendationOptions.toRecommendationOptions(): RecommendationOptions {
        return RecommendationOptions(
            logic = parseJsLogic(this.logic),
            variants = this.variants?.toList(),
            limit = this.limit,
            offset = this.offset,
            filters = parseJsFilters(this.filters),
            availabilityZone = this.availabilityZone,
            language = this.language,
            displayCurrency = this.displayCurrency
        )
    }

    private fun parseJsLogic(jsLogic: String): RecommendationLogic {
        return try {
            when (JsRecommendationLogic.valueOf(jsLogic.uppercase())) {
                JsRecommendationLogic.RELATED -> RecommendationLogic.Related
                JsRecommendationLogic.ALSOBOUGHT -> RecommendationLogic.AlsoBought
                JsRecommendationLogic.CART -> RecommendationLogic.Cart
                JsRecommendationLogic.CATEGORY -> RecommendationLogic.Category
                JsRecommendationLogic.POPULAR -> RecommendationLogic.Popular
                JsRecommendationLogic.SEARCH -> RecommendationLogic.Search
                JsRecommendationLogic.PERSONAL -> RecommendationLogic.Personal
                JsRecommendationLogic.HOME -> RecommendationLogic.Home
            }
        } catch (_: Throwable) {
            RecommendationLogic.Custom(jsLogic)
        }
    }

    private fun parseJsFilters(jsFilters: Array<JsRecommendationFilter>?): List<RecommendationFilter>? {
        return jsFilters?.map { jsFilter ->
            RecommendationFilter(
                mapJsFilterType(jsFilter.type),
                field = jsFilter.field,
                comparison = mapJsComparisonType(jsFilter.comparison),
                expectations = jsFilter.expectations.toList()
            )
        }
    }

    private fun mapJsFilterType(jsFilterType: String): FilterType {
        return try {
            when (JsFilterType.valueOf(jsFilterType.uppercase())) {
                JsFilterType.INCLUDE -> FilterType.INCLUDE
                JsFilterType.EXCLUDE -> FilterType.EXCLUDE
            }
        } catch (_: Throwable) {
            throw IllegalArgumentException("Filter type cannot be mapped: $jsFilterType!")
        }
    }

    private fun mapJsComparisonType(jsComparison: String): ComparisonType {
        return try {
            when (JsComparisonType.valueOf(jsComparison.uppercase())) {
                JsComparisonType.IS -> ComparisonType.IS
                JsComparisonType.IN -> ComparisonType.IN
                JsComparisonType.HAS -> ComparisonType.HAS
                JsComparisonType.OVERLAPS -> ComparisonType.OVERLAPS
            }
        } catch (_: Throwable) {
            throw IllegalArgumentException("Comparison type cannot be mapped: $jsComparison!")
        }
    }
}

