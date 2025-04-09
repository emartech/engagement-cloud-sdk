package com.emarsys.api.predict

import com.emarsys.api.SdkState
import com.emarsys.api.predict.model.PredictCartItem
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter
import com.emarsys.api.predict.model.RecommendationLogic
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PredictTests {

    private companion object {
        const val ORDER_ID = "testOrderId"
        const val ITEM_VIEW = "testItemView"
        const val CATEGORY_VIEW = "testCategoryView"
        const val SEARCH_TERM = "testSearchTerm"
        const val TAG = "testTag"
        const val LIMIT = 60
        const val AVAILABILITY_ZONE = "here"
        val testAttributes = mapOf("key" to "value")
        val testCartItems = listOf(PredictCartItem("testItemId", 1.23, 2.34))
        val testException = Exception()
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
        val testFilters =
            listOf(RecommendationFilter.include("testField").hasValue("otherTestField"))
        val testLogic = RecommendationLogic.alsoBought("itemId")

    }

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    private lateinit var mockLoggingPredict: PredictInstance
    private lateinit var mockGathererPredict: PredictInstance
    private lateinit var mockPredictInternal: PredictInstance
    private lateinit var sdkContext: SdkContextApi
    private lateinit var predict: Predict<PredictInstance, PredictInstance, PredictInstance>

    @BeforeTest
    fun setup() = runTest {
        mockLoggingPredict = mock()
        mockGathererPredict = mock()
        mockPredictInternal = mock()
        
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf(),
            logBreadcrumbsQueueSize = 10
        )

        everySuspend { mockLoggingPredict.activate() } returns Unit
        everySuspend { mockGathererPredict.activate() } returns Unit
        everySuspend { mockPredictInternal.activate() } returns Unit

        predict = Predict(mockLoggingPredict, mockGathererPredict, mockPredictInternal, sdkContext)

        predict.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        
    }

    @Test
    fun testTrackCart_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspend { mockLoggingPredict.trackCart(testCartItems) } returns Unit

        predict.trackCart(testCartItems)

        verifySuspend { mockLoggingPredict.trackCart(testCartItems) }
    }

    @Test
    fun testTrackCart_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspend { mockGathererPredict.trackCart(testCartItems) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        advanceUntilIdle()

        predict.trackCart(testCartItems)

        verifySuspend { mockGathererPredict.trackCart(testCartItems) }
    }

    @Test
    fun testTrackCart_shouldCallGathererInstance_whenActive() = runTest {
        everySuspend { mockPredictInternal.trackCart(testCartItems) } returns Unit

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        predict.trackCart(testCartItems)

        verifySuspend { mockPredictInternal.trackCart(testCartItems) }
    }

    @Test
    fun testTrackCart_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspend { mockPredictInternal.trackCart(testCartItems) } throws testException

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        val result = predict.trackCart(testCartItems)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testTrackPurchase_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspend { mockLoggingPredict.trackPurchase(ORDER_ID, testCartItems) } returns Unit

        predict.trackPurchase(ORDER_ID, testCartItems)

        verifySuspend {
            mockLoggingPredict.trackPurchase(
                ORDER_ID,
                testCartItems
            )
        }
    }

    @Test
    fun testTrackPurchase_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspend { mockGathererPredict.trackPurchase(ORDER_ID, testCartItems) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        advanceUntilIdle()

        predict.trackPurchase(ORDER_ID, testCartItems)

        verifySuspend {
            mockGathererPredict.trackPurchase(
                ORDER_ID,
                testCartItems
            )
        }
    }

    @Test
    fun testTrackPurchase_shouldCallGathererInstance_whenActive() = runTest {
        everySuspend { mockPredictInternal.trackPurchase(ORDER_ID, testCartItems) } returns Unit

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        predict.trackPurchase(ORDER_ID, testCartItems)

        verifySuspend {
            mockPredictInternal.trackPurchase(
                ORDER_ID,
                testCartItems
            )
        }
    }

    @Test
    fun testTrackPurchase_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspend {
            mockPredictInternal.trackPurchase(
                ORDER_ID,
                testCartItems
            )
        } throws testException

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        val result = predict.trackPurchase(ORDER_ID, testCartItems)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testTrackItemView_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspend { mockLoggingPredict.trackItemView(ITEM_VIEW) } returns Unit

        predict.trackItemView(ITEM_VIEW)

        verifySuspend { mockLoggingPredict.trackItemView(ITEM_VIEW) }
    }

    @Test
    fun testTrackItemView_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspend { mockGathererPredict.trackItemView(ITEM_VIEW) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        advanceUntilIdle()

        predict.trackItemView(ITEM_VIEW)

        verifySuspend { mockGathererPredict.trackItemView(ITEM_VIEW) }
    }

    @Test
    fun testTrackItemView_shouldCallGathererInstance_whenActive() = runTest {
        everySuspend { mockPredictInternal.trackItemView(ITEM_VIEW) } returns Unit

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        predict.trackItemView(ITEM_VIEW)

        verifySuspend { mockPredictInternal.trackItemView(ITEM_VIEW) }
    }

    @Test
    fun testTrackItemView_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspend { mockPredictInternal.trackItemView(ITEM_VIEW) } throws testException

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        val result = predict.trackItemView(ITEM_VIEW)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testTrackCategoryView_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspend { mockLoggingPredict.trackCategoryView(CATEGORY_VIEW) } returns Unit

        predict.trackCategoryView(CATEGORY_VIEW)

        verifySuspend { mockLoggingPredict.trackCategoryView(CATEGORY_VIEW) }
    }

    @Test
    fun testTrackCategoryView_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspend { mockGathererPredict.trackCategoryView(CATEGORY_VIEW) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        advanceUntilIdle()

        predict.trackCategoryView(CATEGORY_VIEW)

        verifySuspend { mockGathererPredict.trackCategoryView(CATEGORY_VIEW) }
    }

    @Test
    fun testTrackCategoryView_shouldCallGathererInstance_whenActive() = runTest {
        everySuspend { mockPredictInternal.trackCategoryView(CATEGORY_VIEW) } returns Unit

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        predict.trackCategoryView(CATEGORY_VIEW)

        verifySuspend { mockPredictInternal.trackCategoryView(CATEGORY_VIEW) }
    }

    @Test
    fun testTrackCategoryView_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspend { mockPredictInternal.trackCategoryView(CATEGORY_VIEW) } throws testException

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        val result = predict.trackCategoryView(CATEGORY_VIEW)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testTrackSearchTerm_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspend { mockLoggingPredict.trackSearchTerm(SEARCH_TERM) } returns Unit

        predict.trackSearchTerm(SEARCH_TERM)

        verifySuspend { mockLoggingPredict.trackSearchTerm(SEARCH_TERM) }
    }

    @Test
    fun testTrackSearchTerm_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspend { mockGathererPredict.trackSearchTerm(SEARCH_TERM) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        advanceUntilIdle()

        predict.trackSearchTerm(SEARCH_TERM)

        verifySuspend { mockGathererPredict.trackSearchTerm(SEARCH_TERM) }
    }

    @Test
    fun testTrackSearchTerm_shouldCallGathererInstance_whenActive() = runTest {
        everySuspend { mockPredictInternal.trackSearchTerm(SEARCH_TERM) } returns Unit

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        predict.trackSearchTerm(SEARCH_TERM)

        verifySuspend { mockPredictInternal.trackSearchTerm(SEARCH_TERM) }
    }

    @Test
    fun testTrackSearchTerm_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspend { mockPredictInternal.trackSearchTerm(SEARCH_TERM) } throws testException

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        val result = predict.trackSearchTerm(SEARCH_TERM)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testTrackTag_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspend { mockLoggingPredict.trackTag(TAG, testAttributes) } returns Unit

        predict.trackTag(TAG, testAttributes)

        verifySuspend { mockLoggingPredict.trackTag(TAG, testAttributes) }
    }

    @Test
    fun testTrackTag_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspend { mockGathererPredict.trackTag(TAG, testAttributes) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        advanceUntilIdle()

        predict.trackTag(TAG, testAttributes)

        verifySuspend { mockGathererPredict.trackTag(TAG, testAttributes) }
    }

    @Test
    fun testTrackTag_shouldCallGathererInstance_whenActive() = runTest {
        everySuspend { mockPredictInternal.trackTag(TAG, testAttributes) } returns Unit

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        predict.trackTag(TAG, testAttributes)

        verifySuspend { mockPredictInternal.trackTag(TAG, testAttributes) }
    }

    @Test
    fun testTrackTag_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspend {
            mockPredictInternal.trackTag(
                TAG,
                testAttributes
            )
        } throws testException

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        val result = predict.trackTag(TAG, testAttributes)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testTrackRecommendationClick_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspend { mockLoggingPredict.trackRecommendationClick(testProduct) } returns Unit

        predict.trackRecommendationClick(testProduct)

        verifySuspend {
            mockLoggingPredict.trackRecommendationClick(
                testProduct
            )
        }
    }

    @Test
    fun testTrackRecommendationClick_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspend { mockGathererPredict.trackRecommendationClick(testProduct) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        advanceUntilIdle()

        predict.trackRecommendationClick(testProduct)

        verifySuspend {
            mockGathererPredict.trackRecommendationClick(
                testProduct
            )
        }
    }

    @Test
    fun testTrackRecommendationClick_shouldCallGathererInstance_whenActive() = runTest {
        everySuspend { mockPredictInternal.trackRecommendationClick(testProduct) } returns Unit

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        predict.trackRecommendationClick(testProduct)

        verifySuspend {
            mockPredictInternal.trackRecommendationClick(
                testProduct
            )
        }
    }

    @Test
    fun testTrackRecommendationClick_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspend { mockPredictInternal.trackRecommendationClick(testProduct) } throws testException

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        val result = predict.trackRecommendationClick(testProduct)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testRecommendProducts_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspend {
            mockLoggingPredict.recommendProducts(
                testLogic,
                testFilters,
                LIMIT,
                AVAILABILITY_ZONE
            )
        } returns listOf(testProduct)

        predict.recommendProducts(testLogic, testFilters, LIMIT, AVAILABILITY_ZONE)

        verifySuspend {
            mockLoggingPredict.recommendProducts(
                testLogic,
                testFilters,
                LIMIT,
                AVAILABILITY_ZONE
            )
        }
    }

    @Test
    fun testRecommendProducts_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspend {
            mockGathererPredict.recommendProducts(
                testLogic,
                testFilters,
                LIMIT,
                AVAILABILITY_ZONE
            )
        } returns listOf(testProduct)

        sdkContext.setSdkState(SdkState.onHold)
        advanceUntilIdle()

        predict.recommendProducts(testLogic, testFilters, LIMIT, AVAILABILITY_ZONE)

        verifySuspend {
            mockGathererPredict.recommendProducts(
                testLogic,
                testFilters,
                LIMIT,
                AVAILABILITY_ZONE
            )
        }
    }

    @Test
    fun testRecommendProducts_shouldCallGathererInstance_whenActive() = runTest {
        everySuspend {
            mockPredictInternal.recommendProducts(
                testLogic,
                testFilters,
                LIMIT,
                AVAILABILITY_ZONE
            )
        } returns listOf(
            testProduct
        )

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        predict.recommendProducts(testLogic, testFilters, LIMIT, AVAILABILITY_ZONE)

        verifySuspend {
            mockPredictInternal.recommendProducts(
                testLogic,
                testFilters,
                LIMIT,
                AVAILABILITY_ZONE
            )
        }
    }

    @Test
    fun testRecommendProducts_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspend {
            mockPredictInternal.recommendProducts(
                testLogic,
                testFilters,
                LIMIT,
                AVAILABILITY_ZONE
            )
        } throws testException

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        val result = predict.recommendProducts(testLogic, testFilters, LIMIT, AVAILABILITY_ZONE)

        result.exceptionOrNull() shouldBe testException
    }
}