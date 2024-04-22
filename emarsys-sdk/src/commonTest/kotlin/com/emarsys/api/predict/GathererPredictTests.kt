package com.emarsys.api.predict

import com.emarsys.api.predict.model.PredictCartItem
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter
import com.emarsys.api.predict.model.RecommendationLogic
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererPredictTests {
    private companion object {
        val testCartItems = listOf(PredictCartItem("testItemId", 1.23, 2.34))
    }

    private lateinit var predictContext: PredictContext

    private lateinit var gathererPredict: GathererPredict

    @BeforeTest
    fun setup() = runTest {
        predictContext = PredictContext(mutableListOf())
        gathererPredict = GathererPredict(predictContext)
    }

    @Test
    fun testTrackCart_shouldAddCallToContext() = runTest {
        val expectedCall = PredictCall.TrackCart(testCartItems)

        gathererPredict.trackCart(testCartItems)

        predictContext.calls.contains(expectedCall) shouldBe true
    }

    @Test
    fun testTrackPurchase_shouldAddCallToContext() = runTest {
        val orderId = "testOrderId"
        val expectedCall = PredictCall.TrackPurchase(orderId, testCartItems)

        gathererPredict.trackPurchase(orderId, testCartItems)

        predictContext.calls.contains(expectedCall) shouldBe true
    }

    @Test
    fun testTrackItemView_shouldAddCallToContext() = runTest {
        val itemId = "testItemId"
        val expectedCall = PredictCall.TrackItemView(itemId)

        gathererPredict.trackItemView(itemId)

        predictContext.calls.contains(expectedCall) shouldBe true
    }

    @Test
    fun testTrackCategoryView_shouldAddCallToContext() = runTest {
        val category = "testCategory"
        val expectedCall = PredictCall.TrackCategoryView(category)

        gathererPredict.trackCategoryView(category)

        predictContext.calls.contains(expectedCall) shouldBe true
    }

    @Test
    fun testTrackSearchTerm_shouldAddCallToContext() = runTest {
        val searchTerm = "testSearchTerm"
        val expectedCall = PredictCall.TrackSearchTerm(searchTerm)

        gathererPredict.trackSearchTerm(searchTerm)

        predictContext.calls.contains(expectedCall) shouldBe true
    }

    @Test
    fun testTrackTag_shouldAddCallToContext_withNullAttributes() = runTest {
        val tag = "testTag"
        val attributes: Map<String, String>? = null
        val expectedCall = PredictCall.TrackTag(tag, attributes)

        gathererPredict.trackTag(tag, attributes)

        predictContext.calls.contains(expectedCall) shouldBe true
    }

    @Test
    fun testTrackTag_shouldAddCallToContext_withAttributes() = runTest {
        val tag = "testTag"
        val attributes: Map<String, String> = mapOf("attribute" to "value")
        val expectedCall = PredictCall.TrackTag(tag, attributes)

        gathererPredict.trackTag(tag, attributes)

        predictContext.calls.contains(expectedCall) shouldBe true
    }

    @Test
    fun testTrackRecommendationClick_shouldAddCallToContext() = runTest {
        val testProduct = Product(
            "testId",
            "title",
            "https://google.com",
            "feature",
            "cohort",
            mutableMapOf(),
            "https://image.com",
            zoomImageUrlString = "https://www.image.com"
        )
        val expectedCall = PredictCall.TrackRecommendationClick(testProduct)

        gathererPredict.trackRecommendationClick(testProduct)

        predictContext.calls.contains(expectedCall) shouldBe true
    }

    @Test
    fun testRecommendProducts_shouldAddCallToContext() = runTest {
        val testFilters = listOf(RecommendationFilter.include("testField").hasValue("otherTestField"))
        val testLogic = RecommendationLogic.alsoBought("itemId")
        val testLimit = 60
        val testAvailabilityZone = "here"

        val expectedCall = PredictCall.RecommendProducts(testLogic, testFilters, testLimit, testAvailabilityZone)

        val result = gathererPredict.recommendProducts(testLogic, testFilters, testLimit, testAvailabilityZone)

        predictContext.calls.contains(expectedCall) shouldBe true

        result shouldBe emptyList()
    }

}