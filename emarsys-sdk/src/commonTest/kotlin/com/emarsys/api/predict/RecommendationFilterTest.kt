package com.emarsys.api.predict

import com.emarsys.api.predict.model.RecommendationFilter
import com.emarsys.api.predict.model.RecommendationFilter.Exclude.Companion.exclude
import com.emarsys.api.predict.model.RecommendationFilter.Include.Companion.include
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class RecommendationFilterTest {

    companion object {
        private const val FIELD = "field"
        private const val SINGLE_EXPECTATION = "singleExpectation"
        private val MULTIPLE_EXPECTATIONS = listOf("expectation1", "expectation2")
        private const val EXCLUDE_TYPE = "EXCLUDE"
        private const val INCLUDE_TYPE = "INCLUDE"
    }

    private lateinit var exclude: RecommendationFilter.Exclude
    private lateinit var include: RecommendationFilter.Include


    @BeforeTest
    fun setUp() {
        exclude = exclude(FIELD)
        include = include(FIELD)
    }

    @Test
    fun testExclude_shouldReturn_withExcludeInstance() {
        exclude(FIELD)::class shouldBe RecommendationFilter.Exclude::class
    }

    @Test
    fun testExcludeConstructor_withField() {
        exclude(FIELD).field shouldBe "field"
    }

    @Test
    fun testExclude_is_shouldReturn_RecommendationFilter() {
        exclude.isValue("singleExpectation")::class shouldBe RecommendationFilter::class
    }

    @Test
    fun testExclude_is_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        val expected = RecommendationFilter(EXCLUDE_TYPE, FIELD, "IS", SINGLE_EXPECTATION)
        val result = exclude.isValue("singleExpectation")

        result shouldBe expected
    }

    @Test
    fun testExclude_in_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        val expected = RecommendationFilter(EXCLUDE_TYPE, FIELD, "IN", MULTIPLE_EXPECTATIONS)
        val result = exclude.inValues(listOf("expectation1", "expectation2"))

        result shouldBe expected
    }

    @Test
    fun testExclude_has_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        val expected = RecommendationFilter(EXCLUDE_TYPE, FIELD, "HAS", SINGLE_EXPECTATION)
        val result = exclude.hasValue("singleExpectation")

        result shouldBe expected
    }

    @Test
    fun testExclude_overlaps_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        val expected = RecommendationFilter(EXCLUDE_TYPE, FIELD, "OVERLAPS", MULTIPLE_EXPECTATIONS)
        val result = exclude.overlapsValues(listOf("expectation1", "expectation2"))

        result shouldBe expected
    }

    @Test
    fun testInclude_shouldReturn_withIncludeInstance() {
        include(FIELD)::class shouldBe RecommendationFilter.Include::class
    }

    @Test
    fun testIncludeConstructor_withField() {
        include(FIELD).field shouldBe "field"
    }

    @Test
    fun testInclude_is_shouldReturn_RecommendationFilter() {
        include.isValue("singleExpectation")::class shouldBe RecommendationFilter::class
    }

    @Test
    fun testInclude_is_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        val expected = RecommendationFilter(INCLUDE_TYPE, FIELD, "IS", SINGLE_EXPECTATION)
        val result = include.isValue("singleExpectation")

        result shouldBe expected
    }

    @Test
    fun testInclude_in_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        val expected = RecommendationFilter(INCLUDE_TYPE, FIELD, "IN", MULTIPLE_EXPECTATIONS)
        val result = include.inValues(listOf("expectation1", "expectation2"))

        result shouldBe expected
    }

    @Test
    fun testInclude_has_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        val expected = RecommendationFilter(INCLUDE_TYPE, FIELD, "HAS", SINGLE_EXPECTATION)
        val result = include.hasValue("singleExpectation")

        result shouldBe expected
    }

    @Test
    fun testInclude_overlaps_shouldReturn_RecommendationFilterFilledWithInputParameters() {
        val expected = RecommendationFilter(INCLUDE_TYPE, FIELD, "OVERLAPS", MULTIPLE_EXPECTATIONS)
        val result = include.overlapsValues(listOf("expectation1", "expectation2"))

        result shouldBe expected
    }
}