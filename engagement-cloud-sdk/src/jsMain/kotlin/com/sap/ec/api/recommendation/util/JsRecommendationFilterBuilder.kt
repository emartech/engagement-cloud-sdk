@file:OptIn(ExperimentalJsExport::class)

package com.sap.ec.api.recommendation.util

import com.sap.ec.api.recommendation.model.JsComparisonType
import com.sap.ec.api.recommendation.model.JsFilterType
import com.sap.ec.api.recommendation.model.JsRecommendationFilter


@JsExport
@JsName("RecommendationFilterBuilder")
class JsRecommendationFilterBuilder {

    private lateinit var filterType: String
    private lateinit var field: String
    private lateinit var comparison: String
    private lateinit var expectations: Array<String>

    fun include(): JsRecommendationFilterBuilder {
        filterType = JsFilterType.INCLUDE.name
        return this
    }

    fun exclude(): JsRecommendationFilterBuilder {
        filterType = JsFilterType.EXCLUDE.name
        return this
    }

    fun withField(field: String): JsRecommendationFilterBuilder {
        this.field = field
        return this
    }

    fun withComparisonIs(expectations: Array<String>): JsRecommendationFilterBuilder {
        comparison = JsComparisonType.IS.name
        this.expectations = expectations
        return this
    }

    fun withComparisonIn(expectations: Array<String>): JsRecommendationFilterBuilder {
        comparison = JsComparisonType.IN.name
        this.expectations = expectations
        return this
    }

    fun withComparisonHas(expectations: Array<String>): JsRecommendationFilterBuilder {
        comparison = JsComparisonType.HAS.name
        this.expectations = expectations
        return this
    }

    fun withComparisonOverlaps(expectations: Array<String>): JsRecommendationFilterBuilder {
        comparison = JsComparisonType.OVERLAPS.name
        this.expectations = expectations
        return this
    }

    fun build(): JsRecommendationFilter {
        return try {
            object : JsRecommendationFilter {
                override val type: String = this@JsRecommendationFilterBuilder.filterType
                override val field: String = this@JsRecommendationFilterBuilder.field
                override val comparison: String =
                    this@JsRecommendationFilterBuilder.comparison
                override val expectations: Array<String> =
                    this@JsRecommendationFilterBuilder.expectations
            }
        } catch (_: Throwable) {
            throw IllegalStateException("At least one required field is not set!")
        }
    }
}
