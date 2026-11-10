@file:OptIn(ExperimentalJsExport::class)

package com.sap.ec.api.recommendation.util

import com.sap.ec.api.recommendation.model.JsComparisonType
import com.sap.ec.api.recommendation.model.JsFilterType
import com.sap.ec.api.recommendation.model.JsRecommendationFilter


@JsExport
@JsName("RecommendationFilterBuilder")
class JsRecommendationFilterBuilder {

    private var filterType: String? = null
    private var field: String? = null
    private var comparison: String? = null
    private var expectations: Array<String>? = null

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
        return object : JsRecommendationFilter {
            override val type: String =
                this@JsRecommendationFilterBuilder.filterType ?: throw IllegalStateException("FilterType is not set!")
            override val field: String = this@JsRecommendationFilterBuilder.field ?: throw IllegalStateException("Field is not set!")
            override val comparison: String =
                this@JsRecommendationFilterBuilder.comparison ?: throw IllegalStateException("Comparison is not set!")
            override val expectations: Array<String> =
                this@JsRecommendationFilterBuilder.expectations ?: throw IllegalStateException("Expectations is not set!")
        }
    }
}
