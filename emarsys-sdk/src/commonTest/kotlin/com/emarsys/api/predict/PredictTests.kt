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
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PredictTests : TestsWithMocks() {

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

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockLoggingPredict: PredictInstance

    @Mock
    lateinit var mockGathererPredict: PredictInstance

    @Mock
    lateinit var mockPredictInternal: PredictInstance

    private lateinit var sdkContext: SdkContextApi

    private lateinit var predict: Predict<PredictInstance, PredictInstance, PredictInstance>

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @BeforeTest
    fun setup() = runTest {
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )

        everySuspending { mockLoggingPredict.activate() } returns Unit
        everySuspending { mockGathererPredict.activate() } returns Unit
        everySuspending { mockPredictInternal.activate() } returns Unit

        predict = Predict(mockLoggingPredict, mockGathererPredict, mockPredictInternal, sdkContext)

        predict.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        mocker.reset()
    }

    @Test
    fun testTrackCart_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspending { mockLoggingPredict.trackCart(testCartItems) } returns Unit

        predict.trackCart(testCartItems)

        verifyWithSuspend(exhaustive = false) { mockLoggingPredict.trackCart(testCartItems) }
    }

    @Test
    fun testTrackCart_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspending { mockGathererPredict.trackCart(testCartItems) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        predict.trackCart(testCartItems)

        verifyWithSuspend(exhaustive = false) { mockGathererPredict.trackCart(testCartItems) }
    }

    @Test
    fun testTrackCart_shouldCallGathererInstance_whenActive() = runTest {
        everySuspending { mockPredictInternal.trackCart(testCartItems) } returns Unit

        sdkContext.setSdkState(SdkState.active)

        predict.trackCart(testCartItems)

        verifyWithSuspend(exhaustive = false) { mockPredictInternal.trackCart(testCartItems) }
    }

    @Test
    fun testTrackCart_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspending { mockPredictInternal.trackCart(testCartItems) } runs { throw testException }

        sdkContext.setSdkState(SdkState.active)

        val result = predict.trackCart(testCartItems)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testTrackPurchase_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspending { mockLoggingPredict.trackPurchase(ORDER_ID, testCartItems) } returns Unit

        predict.trackPurchase(ORDER_ID, testCartItems)

        verifyWithSuspend(exhaustive = false) {
            mockLoggingPredict.trackPurchase(
                ORDER_ID,
                testCartItems
            )
        }
    }

    @Test
    fun testTrackPurchase_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspending { mockGathererPredict.trackPurchase(ORDER_ID, testCartItems) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        predict.trackPurchase(ORDER_ID, testCartItems)

        verifyWithSuspend(exhaustive = false) {
            mockGathererPredict.trackPurchase(
                ORDER_ID,
                testCartItems
            )
        }
    }

    @Test
    fun testTrackPurchase_shouldCallGathererInstance_whenActive() = runTest {
        everySuspending { mockPredictInternal.trackPurchase(ORDER_ID, testCartItems) } returns Unit

        sdkContext.setSdkState(SdkState.active)

        predict.trackPurchase(ORDER_ID, testCartItems)

        verifyWithSuspend(exhaustive = false) {
            mockPredictInternal.trackPurchase(
                ORDER_ID,
                testCartItems
            )
        }
    }

    @Test
    fun testTrackPurchase_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspending {
            mockPredictInternal.trackPurchase(
                ORDER_ID,
                testCartItems
            )
        } runs { throw testException }

        sdkContext.setSdkState(SdkState.active)

        val result = predict.trackPurchase(ORDER_ID, testCartItems)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testTrackItemView_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspending { mockLoggingPredict.trackItemView(ITEM_VIEW) } returns Unit

        predict.trackItemView(ITEM_VIEW)

        verifyWithSuspend(exhaustive = false) { mockLoggingPredict.trackItemView(ITEM_VIEW) }
    }

    @Test
    fun testTrackItemView_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspending { mockGathererPredict.trackItemView(ITEM_VIEW) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        predict.trackItemView(ITEM_VIEW)

        verifyWithSuspend(exhaustive = false) { mockGathererPredict.trackItemView(ITEM_VIEW) }
    }

    @Test
    fun testTrackItemView_shouldCallGathererInstance_whenActive() = runTest {
        everySuspending { mockPredictInternal.trackItemView(ITEM_VIEW) } returns Unit

        sdkContext.setSdkState(SdkState.active)

        predict.trackItemView(ITEM_VIEW)

        verifyWithSuspend(exhaustive = false) { mockPredictInternal.trackItemView(ITEM_VIEW) }
    }

    @Test
    fun testTrackItemView_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspending { mockPredictInternal.trackItemView(ITEM_VIEW) } runs { throw testException }

        sdkContext.setSdkState(SdkState.active)

        val result = predict.trackItemView(ITEM_VIEW)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testTrackCategoryView_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspending { mockLoggingPredict.trackCategoryView(CATEGORY_VIEW) } returns Unit

        predict.trackCategoryView(CATEGORY_VIEW)

        verifyWithSuspend(exhaustive = false) { mockLoggingPredict.trackCategoryView(CATEGORY_VIEW) }
    }

    @Test
    fun testTrackCategoryView_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspending { mockGathererPredict.trackCategoryView(CATEGORY_VIEW) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        predict.trackCategoryView(CATEGORY_VIEW)

        verifyWithSuspend(exhaustive = false) { mockGathererPredict.trackCategoryView(CATEGORY_VIEW) }
    }

    @Test
    fun testTrackCategoryView_shouldCallGathererInstance_whenActive() = runTest {
        everySuspending { mockPredictInternal.trackCategoryView(CATEGORY_VIEW) } returns Unit

        sdkContext.setSdkState(SdkState.active)

        predict.trackCategoryView(CATEGORY_VIEW)

        verifyWithSuspend(exhaustive = false) { mockPredictInternal.trackCategoryView(CATEGORY_VIEW) }
    }

    @Test
    fun testTrackCategoryView_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspending { mockPredictInternal.trackCategoryView(CATEGORY_VIEW) } runs { throw testException }

        sdkContext.setSdkState(SdkState.active)

        val result = predict.trackCategoryView(CATEGORY_VIEW)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testTrackSearchTerm_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspending { mockLoggingPredict.trackSearchTerm(SEARCH_TERM) } returns Unit

        predict.trackSearchTerm(SEARCH_TERM)

        verifyWithSuspend(exhaustive = false) { mockLoggingPredict.trackSearchTerm(SEARCH_TERM) }
    }

    @Test
    fun testTrackSearchTerm_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspending { mockGathererPredict.trackSearchTerm(SEARCH_TERM) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        predict.trackSearchTerm(SEARCH_TERM)

        verifyWithSuspend(exhaustive = false) { mockGathererPredict.trackSearchTerm(SEARCH_TERM) }
    }

    @Test
    fun testTrackSearchTerm_shouldCallGathererInstance_whenActive() = runTest {
        everySuspending { mockPredictInternal.trackSearchTerm(SEARCH_TERM) } returns Unit

        sdkContext.setSdkState(SdkState.active)

        predict.trackSearchTerm(SEARCH_TERM)

        verifyWithSuspend(exhaustive = false) { mockPredictInternal.trackSearchTerm(SEARCH_TERM) }
    }

    @Test
    fun testTrackSearchTerm_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspending { mockPredictInternal.trackSearchTerm(SEARCH_TERM) } runs { throw testException }

        sdkContext.setSdkState(SdkState.active)

        val result = predict.trackSearchTerm(SEARCH_TERM)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testTrackTag_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspending { mockLoggingPredict.trackTag(TAG, testAttributes) } returns Unit

        predict.trackTag(TAG, testAttributes)

        verifyWithSuspend(exhaustive = false) { mockLoggingPredict.trackTag(TAG, testAttributes) }
    }

    @Test
    fun testTrackTag_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspending { mockGathererPredict.trackTag(TAG, testAttributes) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        predict.trackTag(TAG, testAttributes)

        verifyWithSuspend(exhaustive = false) { mockGathererPredict.trackTag(TAG, testAttributes) }
    }

    @Test
    fun testTrackTag_shouldCallGathererInstance_whenActive() = runTest {
        everySuspending { mockPredictInternal.trackTag(TAG, testAttributes) } returns Unit

        sdkContext.setSdkState(SdkState.active)

        predict.trackTag(TAG, testAttributes)

        verifyWithSuspend(exhaustive = false) { mockPredictInternal.trackTag(TAG, testAttributes) }
    }

    @Test
    fun testTrackTag_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspending {
            mockPredictInternal.trackTag(
                TAG,
                testAttributes
            )
        } runs { throw testException }

        sdkContext.setSdkState(SdkState.active)

        val result = predict.trackTag(TAG, testAttributes)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testTrackRecommendationClick_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspending { mockLoggingPredict.trackRecommendationClick(testProduct) } returns Unit

        predict.trackRecommendationClick(testProduct)

        verifyWithSuspend(exhaustive = false) {
            mockLoggingPredict.trackRecommendationClick(
                testProduct
            )
        }
    }

    @Test
    fun testTrackRecommendationClick_shouldCallGathererInstance_whenOnHold() = runTest {
        everySuspending { mockGathererPredict.trackRecommendationClick(testProduct) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        predict.trackRecommendationClick(testProduct)

        verifyWithSuspend(exhaustive = false) {
            mockGathererPredict.trackRecommendationClick(
                testProduct
            )
        }
    }

    @Test
    fun testTrackRecommendationClick_shouldCallGathererInstance_whenActive() = runTest {
        everySuspending { mockPredictInternal.trackRecommendationClick(testProduct) } returns Unit

        sdkContext.setSdkState(SdkState.active)

        predict.trackRecommendationClick(testProduct)

        verifyWithSuspend(exhaustive = false) {
            mockPredictInternal.trackRecommendationClick(
                testProduct
            )
        }
    }

    @Test
    fun testTrackRecommendationClick_shouldCallGathererInstance_whenActive_whenThrows() = runTest {
        everySuspending { mockPredictInternal.trackRecommendationClick(testProduct) } runs { throw testException }

        sdkContext.setSdkState(SdkState.active)

        val result = predict.trackRecommendationClick(testProduct)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testRecommendProducts_shouldCallLoggingInstance_whenInactive() = runTest {
        everySuspending {
            mockLoggingPredict.recommendProducts(
                testLogic,
                testFilters,
                LIMIT,
                AVAILABILITY_ZONE
            )
        } returns listOf(testProduct)

        predict.recommendProducts(testLogic, testFilters, LIMIT, AVAILABILITY_ZONE)

        verifyWithSuspend(exhaustive = false) {
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
        everySuspending {
            mockGathererPredict.recommendProducts(
                testLogic,
                testFilters,
                LIMIT,
                AVAILABILITY_ZONE
            )
        } returns listOf(testProduct)

        sdkContext.setSdkState(SdkState.onHold)

        predict.recommendProducts(testLogic, testFilters, LIMIT, AVAILABILITY_ZONE)

        verifyWithSuspend(exhaustive = false) {
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
        everySuspending {
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

        predict.recommendProducts(testLogic, testFilters, LIMIT, AVAILABILITY_ZONE)

        verifyWithSuspend(exhaustive = false) {
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
        everySuspending {
            mockPredictInternal.recommendProducts(
                testLogic,
                testFilters,
                LIMIT,
                AVAILABILITY_ZONE
            )
        } runs { throw testException }

        sdkContext.setSdkState(SdkState.active)

        val result = predict.recommendProducts(testLogic, testFilters, LIMIT, AVAILABILITY_ZONE)

        result.exceptionOrNull() shouldBe testException
    }
}