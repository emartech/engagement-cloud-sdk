package com.sap.ec.api.recommendation.util

import com.sap.ec.api.recommendation.model.JsRecommendationFilter
import com.sap.ec.api.recommendation.model.JsRecommendationOptions
import com.sap.ec.api.recommendation.model.JsRecommendationOptionsConfigApi

internal class JsRecommendationOptionsConfigBuilder(
    private val logic: String
) : JsRecommendationOptionsConfigApi {

    private var variants: Array<String>? = null
    private var limit: Int? = null
    private var offset: Int? = null
    private var filters: Array<JsRecommendationFilter>? = null
    private var availabilityZone: String? = null
    private var language: String? = null
    private var displayCurrency: String? = null

    override fun withVariants(variants: Array<String>): JsRecommendationOptionsConfigApi {
        this.variants = variants
        return this
    }

    override fun withLimit(limit: Int): JsRecommendationOptionsConfigApi {
        this.limit = limit
        return this
    }

    override fun withOffset(offset: Int): JsRecommendationOptionsConfigApi {
        this.offset = offset
        return this
    }

    override fun withFilters(filters: Array<JsRecommendationFilter>): JsRecommendationOptionsConfigApi {
        this.filters = filters
        return this
    }

    override fun withAvailabilityZone(availabilityZone: String): JsRecommendationOptionsConfigApi {
        this.availabilityZone = availabilityZone
        return this
    }

    override fun withLanguage(language: String): JsRecommendationOptionsConfigApi {
        this.language = language
        return this
    }

    override fun withDisplayCurrency(displayCurrency: String): JsRecommendationOptionsConfigApi {
        this.displayCurrency = displayCurrency
        return this
    }

    override fun build(): JsRecommendationOptions {
        return object : JsRecommendationOptions {
            override val logic: String = this@JsRecommendationOptionsConfigBuilder.logic
            override val variants: Array<String>? = this@JsRecommendationOptionsConfigBuilder.variants
            override val limit: Int? = this@JsRecommendationOptionsConfigBuilder.limit
            override val offset: Int? = this@JsRecommendationOptionsConfigBuilder.offset
            override val filters: Array<JsRecommendationFilter>? =
                this@JsRecommendationOptionsConfigBuilder.filters
            override val availabilityZone: String? =
                this@JsRecommendationOptionsConfigBuilder.availabilityZone
            override val language: String? = this@JsRecommendationOptionsConfigBuilder.language
            override val displayCurrency: String? =
                this@JsRecommendationOptionsConfigBuilder.displayCurrency
        }
    }
}
