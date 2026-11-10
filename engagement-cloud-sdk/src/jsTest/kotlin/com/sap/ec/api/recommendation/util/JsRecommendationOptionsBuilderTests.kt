package com.sap.ec.api.recommendation.util

import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test


class JsRecommendationOptionsBuilderTests {
    private companion object {
        const val LIMIT = 10
        const val OFFSET = 100
        const val TEST_ZONE = "testZone"
        const val LANGUAGE = "HU"
        const val CURRENCY = "HUF"
        const val CUSTOM_LOGIC = "myCustomLogic"
        val variants = arrayOf("var1", "var2")
        val testFilter = JsRecommendationFilterBuilder()
            .include()
            .withField("shoes")
            .withComparisonHas(arrayOf("sneaker"))
            .build()
        val filters = arrayOf(testFilter)
    }

    private lateinit var jsRecommendationOptionsBuilder: JsRecommendationOptionsBuilder

    @BeforeTest
    fun setup() {
        jsRecommendationOptionsBuilder = JsRecommendationOptionsBuilder()
    }

    @Test
    fun withLogicRelated_shouldSet_logicToRelated() {
        val result = jsRecommendationOptionsBuilder.withLogicRelated().build()
        result.logic shouldBe "RELATED"
    }

    @Test
    fun withLogicAlsoBought_shouldSet_logicToAlsoBought() {
        val result = jsRecommendationOptionsBuilder.withLogicAlsoBought().build()
        result.logic shouldBe "ALSOBOUGHT"
    }

    @Test
    fun withLogicCart_shouldSet_logicToCart() {
        val result = jsRecommendationOptionsBuilder.withLogicCart().build()
        result.logic shouldBe "CART"
    }

    @Test
    fun withLogicCategory_shouldSet_logicToCategory() {
        val result = jsRecommendationOptionsBuilder.withLogicCategory().build()
        result.logic shouldBe "CATEGORY"
    }

    @Test
    fun withLogicPopular_shouldSet_logicToPopular() {
        val result = jsRecommendationOptionsBuilder.withLogicPopular().build()
        result.logic shouldBe "POPULAR"
    }

    @Test
    fun withLogicSearch_shouldSet_logicToSearch() {
        val result = jsRecommendationOptionsBuilder.withLogicSearch().build()
        result.logic shouldBe "SEARCH"
    }

    @Test
    fun withLogicPersonal_shouldSet_logicToPersonal() {
        val result = jsRecommendationOptionsBuilder.withLogicPersonal().build()
        result.logic shouldBe "PERSONAL"
    }

    @Test
    fun withLogicHome_shouldSet_logicToHome() {
        val result = jsRecommendationOptionsBuilder.withLogicHome().build()
        result.logic shouldBe "HOME"
    }

    @Test
    fun withLogicCustom_shouldSet_logicToProvidedString() {
        val result = jsRecommendationOptionsBuilder.withLogicCustom(CUSTOM_LOGIC).build()
        result.logic shouldBe CUSTOM_LOGIC
    }

    @Test
    fun build_shouldReturnRecommendationOptions_withAllFields() {
        val result = jsRecommendationOptionsBuilder
            .withLogicPopular()
            .withVariants(variants)
            .withLimit(LIMIT)
            .withOffset(OFFSET)
            .withFilters(filters)
            .withAvailabilityZone(TEST_ZONE)
            .withLanguage(LANGUAGE)
            .withDisplayCurrency(CURRENCY)
            .build()

        with(result) {
            logic shouldBe "POPULAR"
            variants shouldBe variants
            limit shouldBe LIMIT
            offset shouldBe OFFSET
            filters shouldBe filters
            availabilityZone shouldBe TEST_ZONE
            language shouldBe LANGUAGE
            displayCurrency shouldBe CURRENCY
        }
    }

    @Test
    fun build_shouldUseDefaultValue_ifSetterIsNotCalled() {
        val result = jsRecommendationOptionsBuilder
            .withLogicPopular()
            .build()

        with(result) {
            logic shouldBe "POPULAR"
            variants shouldBe null
            limit shouldBe null
            offset shouldBe null
            filters shouldBe null
            availabilityZone shouldBe null
            language shouldBe null
            displayCurrency shouldBe null
        }
    }
}