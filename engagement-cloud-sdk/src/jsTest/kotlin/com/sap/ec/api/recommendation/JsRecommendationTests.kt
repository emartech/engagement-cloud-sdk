package com.sap.ec.api.recommendation

import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.api.recommendation.model.JsProduct
import com.sap.ec.api.recommendation.model.JsRecommendationFilter
import com.sap.ec.api.recommendation.model.JsRecommendationOptions
import com.sap.ec.api.tracking.model.recommendation.JsItemViewEvent
import com.sap.ec.recommendation.models.FeatureRecommendation
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.product.Product
import com.sap.ec.recommendation.models.requestRecommendation.ComparisonType
import com.sap.ec.recommendation.models.requestRecommendation.FilterType
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationFilter
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationLogic
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationLogic.Custom
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions
import com.sap.ec.recommendation.models.tracking.ItemViewEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.getIfPresent
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test


@OptIn(ExperimentalWasmJsInterop::class)
class JsRecommendationTests {
    private companion object {
        const val FEATURE = "HOME_1"
        const val MERCHANT = "testMerchant"
        const val TEST_CONTEXT_EVENT_ITEM_ID = "testContextEventItemId"
        const val TOPIC = "testTopic"
        const val LIMIT = 10
        const val OFFSET = 100
        const val LOGIC = "POPULAR"
        const val TEST_ZONE = "testZone"
        const val LANGUAGE = "HU"
        const val CURRENCY = "HUF"
        val testVariants = arrayOf("var1", "var2")
        val testContextEvent = object : JsItemViewEvent {
            override val itemId = TEST_CONTEXT_EVENT_ITEM_ID
        }
        val product1 = Product(
            productId = "2417",
            title = "LSL Women Cardigan Vest - White",
            linkUrl = "https://lifestylelabels.com/lsl-women-cardigan-vest-white.html",
            customFields = emptyMap(),
            imageUrl = "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
            zoomImageUrl = "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
            categoryPath = "WOMEN>Tops & Shirts|WOMEN>Hoodies|Cardigans & Jumpers",
            available = true,
            description = "LSL Women Cardigan Vest - White",
            price = 75.0,
            msrp = 75.0,
            brand = null,
            feature = FEATURE,
            cohort = "AAAA"
        )
        val product2 = Product(
            productId = "2345",
            title = "LSL Women Long Trouser Artistic Pink Pattern",
            linkUrl = "https://lifestylelabels.com/lsl-women-long-trouser-artistic-pink-pattern.html",
            customFields = emptyMap(),
            imageUrl = "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg",
            zoomImageUrl = "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg",
            categoryPath = "WOMEN>Trousers",
            available = true,
            description = "LSL Women Long Trouser Artistic Pink Pattern",
            price = 650.0,
            msrp = 650.0,
            brand = null,
            feature = FEATURE,
            cohort = "AAAA"
        )
        val recommendationResult = RecommendationResult(
            mapOf(
                FEATURE to FeatureRecommendation(
                    FEATURE,
                    topicLabel = TOPIC,
                    hasMore = false,
                    merchants = listOf(MERCHANT),
                    products = listOf(product1, product2)
                )
            )
        )
    }

    private lateinit var mockRecommendation: RecommendationApi
    private lateinit var jsRecommendation: JsRecommendationApi
    private lateinit var jsRecommendationOptions: JsRecommendationOptions

    @BeforeTest
    fun setup() {
        mockRecommendation = mock(MockMode.autofill)
        everySuspend {
            mockRecommendation.requestRecommendation(any(), any())
        } returns Result.success(recommendationResult)
        jsRecommendation = JsRecommendation(mockRecommendation)
        jsRecommendationOptions = object : JsRecommendationOptions {
            override val logic: String = LOGIC
            override val variants: Array<String> = testVariants
            override val limit: Int = LIMIT
            override val offset: Int = OFFSET
            override val filters: Array<JsRecommendationFilter> = arrayOf()
            override val availabilityZone: String = TEST_ZONE
            override val language: String = LANGUAGE
            override val displayCurrency: String = CURRENCY
        }
    }

    @Test
    fun requestRecommendation_shouldReturn_JsRecommendationResult_withFeatures() = runTest {
        val result =
            jsRecommendation.requestRecommendation(jsRecommendationOptions, testContextEvent)

        result.recommendationsByFeature.size shouldBe 1

        with(result.recommendationsByFeature[FEATURE]) {
            this?.let {
                featureName shouldBe FEATURE
                topicLabel shouldBe TOPIC
                hasMore shouldBe false
                merchants shouldBe arrayOf(MERCHANT)
                products.length shouldBe 2

                products.find { it.productId == product1.productId }?.let {
                    validateJsProductFields(it, product1)
                } ?: throw AssertionError("Product1 is missing!")

                products.find { it.productId == product2.productId }?.let {
                    validateJsProductFields(it, product2)
                } ?: throw AssertionError("Product2 is missing!")

            } ?: throw AssertionError("Mapped feature is null.")
        }
    }

    @Test
    fun requestRecommendation_shouldThrow_ifExceptionHappens_duringRecommendationRequest() =
        runTest {
            everySuspend {
                mockRecommendation.requestRecommendation(any(), any())
            } returns Result.failure(RuntimeException("test exception"))

            shouldThrow<RuntimeException> {
                jsRecommendation.requestRecommendation(jsRecommendationOptions)
            }
        }

    @Test
    fun requestRecommendation_shouldCallRecommendation_withCorrectlyParsedValues() = runTest {
        val testFilter: JsRecommendationFilter = object : JsRecommendationFilter {
            override val type = "INCLUDE"
            override val field = "shoes"
            override val comparison = "HAS"
            override val expectations = arrayOf("sneaker")
        }
        val filters = arrayOf(testFilter)
        val jsRecommendationOptions: JsRecommendationOptions = object : JsRecommendationOptions {
            override val logic: String = LOGIC
            override val variants: Array<String> = testVariants
            override val limit: Int = LIMIT
            override val offset: Int = OFFSET
            override val filters: Array<JsRecommendationFilter> = filters
            override val availabilityZone: String = TEST_ZONE
            override val language: String = LANGUAGE
            override val displayCurrency: String = CURRENCY
        }

        val expectedFilter = RecommendationFilter(
            FilterType.INCLUDE,
            comparison = ComparisonType.HAS,
            field = "shoes",
            expectations = listOf("sneaker")
        )
        val expectedContextEvent = ItemViewEvent(TEST_CONTEXT_EVENT_ITEM_ID)
        val expectedRecommendationOption = RecommendationOptions(
            logic = RecommendationLogic.Popular,
            variants = testVariants.toList(),
            limit = LIMIT,
            offset = OFFSET,
            filters = listOf(expectedFilter),
            availabilityZone = TEST_ZONE,
            language = LANGUAGE,
            displayCurrency = CURRENCY
        )
        val optionsSlot = slot<RecommendationOptions>()
        val contextEventSlot = slot<TrackedEvent>()

        everySuspend {
            mockRecommendation.requestRecommendation(
                capture(optionsSlot),
                capture(contextEventSlot)
            )
        } returns Result.success(recommendationResult)

        jsRecommendation.requestRecommendation(jsRecommendationOptions, testContextEvent)

        optionsSlot.getIfPresent() shouldBe expectedRecommendationOption
        contextEventSlot.getIfPresent() shouldBe expectedContextEvent
    }

    @Test
    fun requestRecommendation_shouldCallRecommendation_withCorrectlyParsedValues_customLogic() =
        runTest {
            val customLogic = "some custom logic"
            val jsRecommendationOptions = object : JsRecommendationOptions {
                override val logic = customLogic
                override val variants = null
                override val limit = LIMIT
                override val offset = OFFSET
                override val filters = null
                override val availabilityZone = null
                override val language = null
                override val displayCurrency = null
            }

            val optionsSlot = slot<RecommendationOptions>()

            everySuspend {
                mockRecommendation.requestRecommendation(capture(optionsSlot), any())
            } returns Result.success(recommendationResult)

            jsRecommendation.requestRecommendation(jsRecommendationOptions, testContextEvent)

            optionsSlot.getIfPresent()?.logic shouldBe Custom(customLogic)
        }

    @Test
    fun requestRecommendation_shouldThrow_ifCalledWithInvalidType_comparisonType() = runTest {
        val invalidComparison = "invalid"
        val invalidFilter = object : JsRecommendationFilter {
            override val type = "INCLUDE"
            override val field = "shoes"
            override val comparison = invalidComparison
            override val expectations = arrayOf("sneaker")

        }
        val filters = arrayOf<JsRecommendationFilter>(invalidFilter)
        val jsRecommendationOptions = object : JsRecommendationOptions {
            override val logic = LOGIC
            override val variants = null
            override val limit = LIMIT
            override val offset = OFFSET
            override val filters = filters
            override val availabilityZone = null
            override val language = null
            override val displayCurrency = null
        }

        val error = shouldThrow<IllegalArgumentException> {
            jsRecommendation.requestRecommendation(jsRecommendationOptions, testContextEvent)
        }

        error.message shouldBe "Comparison type cannot be mapped: $invalidComparison!"
    }

    @Test
    fun requestRecommendation_shouldThrow_ifCalledWithInvalidType_filterType() = runTest {
        val invalidFilterType = "invalid"
        val invalidFilter = object : JsRecommendationFilter {
            override val type = invalidFilterType
            override val field = "shoes"
            override val comparison = "HAS"
            override val expectations = arrayOf("sneaker")
        }
        val filters = arrayOf<JsRecommendationFilter>(invalidFilter)
        val jsRecommendationOptions = object : JsRecommendationOptions {
            override val logic = LOGIC
            override val variants = null
            override val limit = LIMIT
            override val offset = OFFSET
            override val filters = filters
            override val availabilityZone = null
            override val language = null
            override val displayCurrency = null
        }

        val error = shouldThrow<IllegalArgumentException> {
            jsRecommendation.requestRecommendation(jsRecommendationOptions, testContextEvent)
        }

        error.message shouldBe "Filter type cannot be mapped: $invalidFilterType!"
    }

    private fun validateJsProductFields(actual: JsProduct, expected: Product) {
        with(actual) {
            productId shouldBe expected.productId
            title shouldBe expected.title
            linkUrl shouldBe expected.linkUrl
            customFields shouldBe expected.customFields
            imageUrl shouldBe expected.imageUrl
            zoomImageUrl shouldBe expected.zoomImageUrl
            categoryPath shouldBe expected.categoryPath
            available shouldBe expected.available
            description shouldBe expected.description
            price shouldBe expected.price
            msrp shouldBe expected.msrp
            brand shouldBe expected.brand
            feature shouldBe expected.feature
            cohort shouldBe expected.cohort
        }
    }
}