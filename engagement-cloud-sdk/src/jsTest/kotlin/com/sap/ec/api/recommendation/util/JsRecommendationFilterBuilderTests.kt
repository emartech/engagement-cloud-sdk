package com.sap.ec.api.recommendation.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class JsRecommendationFilterBuilderTests {

    private lateinit var recommendationFilterBuilder: JsRecommendationFilterBuilder

    @BeforeTest
    fun setup() {
        recommendationFilterBuilder = JsRecommendationFilterBuilder()
    }

    @Test
    fun include_shouldSet_typeToInclude() {
        val result = recommendationFilterBuilder
            .include()
            .withField("brand")
            .withComparisonHas(arrayOf("nike"))
            .build()

        result.type shouldBe "INCLUDE"
    }

    @Test
    fun exclude_shouldSet_typeToExclude() {
        val result = recommendationFilterBuilder
            .exclude()
            .withField("brand")
            .withComparisonHas(arrayOf("nike"))
            .build()

        result.type shouldBe "EXCLUDE"
    }

    @Test
    fun withComparisonIs_shouldSet_comparisonToIs_andExpectations() {
        val result = recommendationFilterBuilder
            .include()
            .withField("brand")
            .withComparisonIs(arrayOf("nike"))
            .build()

        result.comparison shouldBe "IS"
        result.expectations shouldBe arrayOf("nike")
    }

    @Test
    fun withComparisonIn_shouldSet_comparisonToIn_andExpectations() {
        val result = recommendationFilterBuilder
            .include()
            .withField("brand")
            .withComparisonIn(arrayOf("nike"))
            .build()

        result.comparison shouldBe "IN"
        result.expectations shouldBe arrayOf("nike")
    }

    @Test
    fun withComparisonHas_shouldSet_comparisonToHas_andExpectations() {
        val result = recommendationFilterBuilder
            .include()
            .withField("brand")
            .withComparisonHas(arrayOf("nike"))
            .build()

        result.comparison shouldBe "HAS"
        result.expectations shouldBe arrayOf("nike")
    }

    @Test
    fun withComparisonOverlaps_shouldSet_comparisonToOverlaps_andExpectations() {
        val result = recommendationFilterBuilder
            .include()
            .withField("brand")
            .withComparisonOverlaps(arrayOf("nike"))
            .build()

        result.comparison shouldBe "OVERLAPS"
        result.expectations shouldBe arrayOf("nike")
    }

    @Test
    fun build_shouldReturn_recommendationFilter_withAllFields() {
        val result = recommendationFilterBuilder
            .include()
            .withField("shoes")
            .withComparisonHas(arrayOf("sneaker"))
            .build()

        with(result) {
            type shouldBe "INCLUDE"
            comparison shouldBe "HAS"
            field shouldBe "shoes"
            expectations shouldBe arrayOf("sneaker")
        }
    }

    @Test
    fun build_shouldThrowException_ifType_isMissing() {
        shouldThrow<IllegalStateException> {
            recommendationFilterBuilder
                .withField("shoes")
                .withComparisonHas(arrayOf("sneaker"))
                .build()
        }
    }

    @Test
    fun build_shouldThrowException_ifComparison_isMissing() {
        shouldThrow<IllegalStateException> {
            recommendationFilterBuilder
                .include()
                .withField("shoes")
                .build()
        }
    }

    @Test
    fun build_shouldThrowException_ifField_isMissing() {
        shouldThrow<IllegalStateException> {
            recommendationFilterBuilder
                .include()
                .withComparisonHas(arrayOf("sneaker"))
                .build()
        }
    }
}