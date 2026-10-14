package com.sap.ec.api.recommendation

import com.sap.ec.api.recommendation.model.JsProduct
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.product.Product
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationLogic
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
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
        const val TOPIC = "testTopic"
        val testRequestRecommendationEvent = SdkEvent.Internal.Sdk.RequestRecommendation(
            options = RecommendationOptions(
                RecommendationLogic.Home
            )
        )
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
    }

    private lateinit var mockRecommendation: RecommendationApi
    private lateinit var jsRecommendation: JsRecommendationApi

    @BeforeTest
    fun setup() {
        mockRecommendation = mock(MockMode.autofill)
        jsRecommendation = JsRecommendation(mockRecommendation)
    }

    @Test
    fun requestRecommendation_shouldReturn_mapOfFeatures_andJsRecommendationResults() = runTest {
        val recommendationResultMap = mapOf(
            FEATURE to RecommendationResult(
                FEATURE,
                topicLabel = TOPIC,
                hasMore = false,
                merchants = listOf(MERCHANT),
                products = listOf(product1, product2)
            )
        )

        everySuspend { mockRecommendation.requestRecommendation(any()) } returns Result.success(
            recommendationResultMap
        )

        val result = jsRecommendation.requestRecommendation(testRequestRecommendationEvent)

        result.size shouldBe 1

        with(result[FEATURE]) {
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
            everySuspend { mockRecommendation.requestRecommendation(any()) } returns Result.failure(
                RuntimeException("test exception")
            )

            shouldThrow<RuntimeException> {
                jsRecommendation.requestRecommendation(testRequestRecommendationEvent)
            }
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