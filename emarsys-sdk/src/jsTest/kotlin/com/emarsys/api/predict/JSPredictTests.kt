package com.emarsys.api.predict

import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JSPredictTests {
    private companion object {
        const val ORDER_ID = "orderId"
        const val ITEM_ID = "itemId"
        const val CATEGORY_PATH = "categoryPath"
        const val SEARCH_TERM = "searchTerm"
        const val TAG = "tag"
        val attributes = mapOf("key" to "value")
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
        val mockCartItem1: CartItem = mock()
        val mockCartItem2: CartItem = mock()
        val testCartItems = listOf(mockCartItem1, mockCartItem2)
        val mockLogic: Logic = mock()
        val testFilter: RecommendationFilter =
            RecommendationFilter.include("testField").hasValue("value")
        val successResult = Result.success(Unit)
        val failureResult = Result.failure<Unit>(Exception())

    }

    private lateinit var jsPredict: JSPredictApi
    private lateinit var mockPredictApi: PredictApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockPredictApi = mock()
        jsPredict = JSPredict(mockPredictApi, TestScope())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun trackCart_shouldCall_trackCartOnPredictApi() = runTest {
        everySuspend { mockPredictApi.trackCart(testCartItems) } returns successResult

        jsPredict.trackCart(testCartItems).await()

        verifySuspend { mockPredictApi.trackCart(testCartItems) }
    }

    @Test
    fun trackCart_shouldThrowException_ifTrackCartOnPredictApi_fails() = runTest {
        everySuspend { mockPredictApi.trackCart(testCartItems) } returns failureResult

        shouldThrow<Exception> { jsPredict.trackCart(testCartItems).await() }
    }

    @Test
    fun trackPurchase_shouldCall_trackPurchaseOnPredictApi() = runTest {
        everySuspend { mockPredictApi.trackPurchase(ORDER_ID, testCartItems) } returns successResult

        jsPredict.trackPurchase(ORDER_ID, testCartItems).await()

        verifySuspend { mockPredictApi.trackPurchase(ORDER_ID, testCartItems) }
    }

    @Test
    fun trackPurchase_shouldThrowException_ifTrackPurchaseOnPredictApi_fails() = runTest {
        everySuspend { mockPredictApi.trackPurchase(ORDER_ID, testCartItems) } returns failureResult

        shouldThrow<Exception> { jsPredict.trackPurchase(ORDER_ID, testCartItems).await() }
    }

    @Test
    fun trackItemView_shouldCall_trackItemViewOnPredictApi() = runTest {
        everySuspend { mockPredictApi.trackItemView(ITEM_ID) } returns successResult

        jsPredict.trackItemView(ITEM_ID).await()

        verifySuspend { mockPredictApi.trackItemView(ITEM_ID) }
    }

    @Test
    fun trackItemView_shouldThrowException_ifTrackItemViewOnPredictApi_fails() = runTest {
        everySuspend { mockPredictApi.trackItemView(ITEM_ID) } returns failureResult

        shouldThrow<Exception> { jsPredict.trackItemView(ITEM_ID).await() }
    }

    @Test
    fun trackCategoryView_shouldCall_trackCategoryViewOnPredictApi() = runTest {
        everySuspend { mockPredictApi.trackCategoryView(CATEGORY_PATH) } returns successResult

        jsPredict.trackCategoryView(CATEGORY_PATH).await()

        verifySuspend { mockPredictApi.trackCategoryView(CATEGORY_PATH) }
    }

    @Test
    fun trackCategoryView_shouldThrowException_ifTrackCategoryViewOnPredictApi_fails() = runTest {
        everySuspend { mockPredictApi.trackCategoryView(CATEGORY_PATH) } returns failureResult

        shouldThrow<Exception> { jsPredict.trackCategoryView(CATEGORY_PATH).await() }
    }

    @Test
    fun trackSearchTerm_shouldCall_trackSearchTermOnPredictApi() = runTest {
        everySuspend { mockPredictApi.trackSearchTerm(SEARCH_TERM) } returns successResult

        jsPredict.trackSearchTerm(SEARCH_TERM).await()

        verifySuspend { mockPredictApi.trackSearchTerm(SEARCH_TERM) }
    }

    @Test
    fun trackSearchTerm_shouldThrowException_ifTrackSearchTermOnPredictApi_fails() = runTest {
        everySuspend { mockPredictApi.trackSearchTerm(SEARCH_TERM) } returns failureResult

        shouldThrow<Exception> { jsPredict.trackSearchTerm(SEARCH_TERM).await() }
    }

    @Test
    fun trackTag_shouldCall_trackTagOnPredictApi() = runTest {
        everySuspend { mockPredictApi.trackTag(TAG, attributes) } returns successResult

        jsPredict.trackTag(TAG, attributes).await()

        verifySuspend { mockPredictApi.trackTag(TAG, attributes) }
    }

    @Test
    fun trackTag_shouldThrowException_iftrackTagOnPredictApi_fails() = runTest {
        everySuspend { mockPredictApi.trackTag(TAG, attributes) } returns failureResult

        shouldThrow<Exception> { jsPredict.trackTag(TAG, attributes).await() }
    }

    @Test
    fun trackRecommendationClick_shouldCall_trackRecommendationClickOnPredictApi() = runTest {
        everySuspend { mockPredictApi.trackRecommendationClick(testProduct) } returns successResult

        jsPredict.trackRecommendationClick(testProduct).await()

        verifySuspend { mockPredictApi.trackRecommendationClick(testProduct) }
    }

    @Test
    fun trackRecommendationClick_shouldThrowException_ifTrackRecommendationClickOnPredictApi_fails() =
        runTest {
            everySuspend { mockPredictApi.trackRecommendationClick(testProduct) } returns failureResult

            shouldThrow<Exception> { jsPredict.trackRecommendationClick(testProduct).await() }
        }

    @Test
    fun recommendProducts_shouldCall_recommendProductsOnPredictApi() = runTest {
        val result = Result.success(listOf(testProduct))
        val testLimit = 12
        val testZone = "testZone"
        everySuspend {
            mockPredictApi.recommendProducts(
                mockLogic,
                listOf(testFilter),
                testLimit,
                testZone
            )
        } returns result

        val recommendationResult = jsPredict.recommendProducts(
            mockLogic,
            listOf(testFilter),
            testLimit,
            testZone
        ).await()

        recommendationResult shouldBe listOf(testProduct)
    }

    @Test
    fun recommendProducts_shouldThrowException_ifRecommendProductsOnPredictApi_fails() = runTest {
        val result = Result.failure<List<Product>>(Exception())
        val testLimit = 12
        val testZone = "testZone"
        everySuspend {
            mockPredictApi.recommendProducts(
                mockLogic,
                listOf(testFilter),
                testLimit,
                testZone
            )
        } returns result

        shouldThrow<Exception> {
            jsPredict.recommendProducts(
                mockLogic,
                listOf(testFilter),
                testLimit,
                testZone
            ).await()
        }
    }
}