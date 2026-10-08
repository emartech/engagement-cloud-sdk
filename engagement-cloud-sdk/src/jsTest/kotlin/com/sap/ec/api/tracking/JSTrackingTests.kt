package com.sap.ec.api.tracking

import com.sap.ec.api.event.model.CustomEvent
import com.sap.ec.api.event.model.NavigateEvent
import com.sap.ec.api.tracking.model.JsCustomEvent
import com.sap.ec.api.tracking.model.JsNavigateEvent
import com.sap.ec.api.tracking.model.recommendation.JSCategoryViewEvent
import com.sap.ec.api.tracking.model.recommendation.JsCartEvent
import com.sap.ec.api.tracking.model.recommendation.JsCartItem
import com.sap.ec.api.tracking.model.recommendation.JsItemViewEvent
import com.sap.ec.api.tracking.model.recommendation.JsPurchaseEvent
import com.sap.ec.api.tracking.model.recommendation.JsSearchEvent
import com.sap.ec.api.tracking.model.recommendation.JsTagEvent
import com.sap.ec.recommendation.models.CartItem
import com.sap.ec.recommendation.models.tracking.CartEvent
import com.sap.ec.recommendation.models.tracking.CategoryViewEvent
import com.sap.ec.recommendation.models.tracking.ItemViewEvent
import com.sap.ec.recommendation.models.tracking.PurchaseEvent
import com.sap.ec.recommendation.models.tracking.SearchEvent
import com.sap.ec.recommendation.models.tracking.TagEvent
import com.sap.ec.tracking.TrackingApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JSTrackingTests {
    private companion object {
        const val RECOMMENDATION_EVENT_TYPE = "RECOMMENDATION"
        val successResult: Result<Unit> = Result.success(Unit)
    }

    private lateinit var jsTracking: JSTrackingApi
    private lateinit var mockEventTrackerApi: TrackingApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockEventTrackerApi = mock(MockMode.autofill)
        everySuspend { mockEventTrackerApi.track(any()) } returns successResult
        jsTracking = JSTracking(mockEventTrackerApi, sdkLogger = mock(MockMode.autofill))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun track_shouldCall_track_onEventTrackerApi_withCustomEvent() =
        runTest {
            val testName = "testName"
            val testAttributesMap = mapOf("testKey" to "testValue")

            val jsCustomEvent: JsCustomEvent =
                js("{ type: 'CUSTOM', name: testName, attributes: { 'testKey': 'testValue' } }").unsafeCast<JsCustomEvent>()

            jsTracking.track(jsCustomEvent)

            verifySuspend { mockEventTrackerApi.track(CustomEvent(testName, testAttributesMap)) }
        }

    @Test
    fun track_shouldCall_track_onEventTrackerApi_withCustomEvent_withLowercase_eventType() =
        runTest {
            val testName = "testName"
            val testAttributesMap = mapOf("testKey" to "testValue")

            val jsCustomEvent: JsCustomEvent =
                js("{ type: 'custom', name: testName, attributes: { 'testKey': 'testValue' } }").unsafeCast<JsCustomEvent>()

            jsTracking.track(jsCustomEvent)

            verifySuspend { mockEventTrackerApi.track(CustomEvent(testName, testAttributesMap)) }
        }

    @Test
    fun track_shouldCall_track_onEventTrackerApi_withCustomEvent_withoutAttributes() =
        runTest {
            val testName = "testName"

            val jsCustomEvent: JsCustomEvent =
                js("{ type: 'custom', name: testName }").unsafeCast<JsCustomEvent>()

            jsTracking.track(jsCustomEvent)

            verifySuspend { mockEventTrackerApi.track(CustomEvent(testName)) }
        }

    @Test
    fun track_shouldCall_track_onEventTrackerApi_withNavigateEvent() =
        runTest {
            val location = "https://example.com"
            val jsNavigateEvent: JsNavigateEvent =
                js("{ type: 'NAVIGATE', location: location }").unsafeCast<JsNavigateEvent>()

            jsTracking.track(jsNavigateEvent)

            verifySuspend { mockEventTrackerApi.track(NavigateEvent(location)) }
        }

    @Test
    fun track_shouldThrowException_ifEventIsMissingARequiredProperty() =
        runTest {
            val jsNavigateEvent: JsNavigateEvent =
                js("{ type: 'NAVIGATE', location: undefined }").unsafeCast<JsNavigateEvent>()

            val exception =
                shouldThrow<IllegalArgumentException> { jsTracking.track(jsNavigateEvent) }

            exception.message shouldBe "Failed to parse event."
        }

    @Test
    fun track_shouldThrowException_ifInvalidEventTypeIsPassed() = runTest {
        val jsNavigateEvent: JsNavigateEvent =
            js("{ type: 'TEST', location: 'location' }").unsafeCast<JsNavigateEvent>()

        val exception = shouldThrow<IllegalArgumentException> { jsTracking.track(jsNavigateEvent) }

        exception.message shouldBe "Invalid event type: TEST. Valid types are: custom, navigate, recommendation"
    }

    @Test
    fun track_shouldThrowException_ifPayloadMappingFails() = runTest {
        val customEvent: JsCustomEvent =
            js("{ type: 'CUSTOM', name: 'testName', attributes: { 'testKey': {} }}").unsafeCast<JsCustomEvent>()

        val exception = shouldThrow<IllegalArgumentException> { jsTracking.track(customEvent) }
        exception.message shouldBe "Failed to parse event."
        exception.cause?.message shouldBe "Failed to parse attributes map."
    }

    @Test
    fun track_shouldThrowException_eventTypeIsUnknown() = runTest {
        val customEvent: JsCustomEvent =
            js("{ type: 'unknown', name: 'testName', attributes: { 'testKey': {} }}").unsafeCast<JsCustomEvent>()

        val exception = shouldThrow<IllegalArgumentException> { jsTracking.track(customEvent) }
        exception.message shouldBe "Invalid event type: unknown. Valid types are: custom, navigate, recommendation"
    }

    @Test
    fun track_shouldThrowException_ifEventTrackingFails() = runTest {
        val testName = "testName"
        val testAttributesMap = mapOf("testKey" to "testValue")
        val customEvent: JsCustomEvent =
            js("{ type: 'CUSTOM', name: testName, attributes: { 'testKey': 'testValue' } }").unsafeCast<JsCustomEvent>()
        val testExceptionMessage = "Tracking failed"

        everySuspend {
            mockEventTrackerApi.track(CustomEvent(testName, testAttributesMap))
        } returns Result.failure(Exception(testExceptionMessage))

        val exception = shouldThrow<Exception> { jsTracking.track(customEvent) }
        exception.message shouldBe testExceptionMessage
    }

    @Test
    fun track_shouldThrowException_ifEventValidationFails_listOfItemIds_isPassed() = runTest {
        val itemViewEvent: JsItemViewEvent =
            js("{ type: '$RECOMMENDATION_EVENT_TYPE', recommendationEventType: 'ITEM_VIEW', itemId: ['itemId'] }").unsafeCast<JsItemViewEvent>()

        val exception = shouldThrow<IllegalArgumentException> { jsTracking.track(itemViewEvent) }

        exception.message shouldBe "Failed to parse event."
    }

    @Test
    fun track_shouldCallTrack_onEventTracker_withItemViewEvent() = runTest {
        val itemId = "testItemId"
        val itemViewEvent: JsItemViewEvent =
            js("{ type: '$RECOMMENDATION_EVENT_TYPE', recommendationEventType: 'ITEM_VIEW', itemId: itemId }").unsafeCast<JsItemViewEvent>()

        jsTracking.track(itemViewEvent)

        everySuspend { mockEventTrackerApi.track(ItemViewEvent(itemId)) }
    }

    @Test
    fun track_shouldCallTrack_onEventTracker_withCategoryViewEvent() = runTest {
        val category = "testCategory"
        val categoryViewEvent: JSCategoryViewEvent =
            js("{ type: '$RECOMMENDATION_EVENT_TYPE', recommendationEventType: 'CATEGORY_VIEW', categoryPath: category }").unsafeCast<JSCategoryViewEvent>()

        jsTracking.track(categoryViewEvent)

        everySuspend { mockEventTrackerApi.track(CategoryViewEvent(category)) }
    }

    @Test
    fun track_shouldCallTrack_onEventTracker_withSearchEvent() = runTest {
        val testSearchTerm = "testSearch"
        val searchEvent: JsSearchEvent =
            js("{ type: '$RECOMMENDATION_EVENT_TYPE', recommendationEventType: 'SEARCH', searchTerm: testSearchTerm }").unsafeCast<JsSearchEvent>()

        jsTracking.track(searchEvent)

        verifySuspend { mockEventTrackerApi.track(SearchEvent(testSearchTerm)) }
    }

    @Test
    fun track_shouldCallTrack_onEventTracker_withCartEvent() = runTest {
        val item1 = js("{itemId: 'item1', price: 123.456 , quantity: 99}").unsafeCast<JsCartItem>()
        val item2 = js("{itemId: 'item2', price: 45.78 , quantity: 11.5}").unsafeCast<JsCartItem>()
        val expectedItems = listOf(
            CartItem("item1", 123.456, 99.toDouble()),
            CartItem("item2", 45.78, 11.5)
        )
        val cartEvent: JsCartEvent =
            js("{ type: '$RECOMMENDATION_EVENT_TYPE', recommendationEventType: 'CART', items: [item1, item2] }").unsafeCast<JsCartEvent>()

        jsTracking.track(cartEvent)

        verifySuspend { mockEventTrackerApi.track(CartEvent(expectedItems)) }
    }

    @Test
    fun track_shouldCallTrack_onEventTracker_withPurchaseEvent() = runTest {
        val testOrderId = "testOrderId"
        val item1 = js("{itemId: 'item1', price: 123.456 , quantity: 99}").unsafeCast<JsCartItem>()
        val item2 = js("{itemId: 'item2', price: 45.78 , quantity: 11.5}").unsafeCast<JsCartItem>()
        val expectedItems = listOf(
            CartItem("item1", 123.456, 99.toDouble()),
            CartItem("item2", 45.78, 11.5)
        )
        val purchaseEvent: JsPurchaseEvent =
            js("{ type: '$RECOMMENDATION_EVENT_TYPE', recommendationEventType: 'PURCHASE', orderId: testOrderId, items: [item1, item2] }").unsafeCast<JsPurchaseEvent>()

        jsTracking.track(purchaseEvent)

        verifySuspend { mockEventTrackerApi.track(PurchaseEvent(testOrderId, expectedItems)) }
    }

    @Test
    fun track_shouldCallTrack_onEventTracker_withTagEvent() = runTest {
        val testTag = "testMyTag"
        val purchaseEvent: JsTagEvent =
            js("{ type: '$RECOMMENDATION_EVENT_TYPE', recommendationEventType: 'TAG', tag: testTag, attributes: { 'testKey': 'testValue' } }").unsafeCast<JsTagEvent>()

        jsTracking.track(purchaseEvent)

        verifySuspend {
            mockEventTrackerApi.track(
                TagEvent(testTag, mapOf("testKey" to "testValue"))
            )
        }
    }

    @Test
    fun track_shouldCallTrack_onEventTracker_withTagEvent_withNullAttribute_ifAttributesAreEmpty() =
        runTest {
            val testTag = "testMyTag"
            val jsTagEvent: JsTagEvent =
                js("{ type: '$RECOMMENDATION_EVENT_TYPE', recommendationEventType: 'TAG', tag: testTag, attributes: { } }").unsafeCast<JsTagEvent>()

            jsTracking.track(jsTagEvent)

            verifySuspend { mockEventTrackerApi.track(TagEvent(testTag, null)) }
        }

    @Test
    fun track_shouldThrowException_whenRecommendationEventType_isNotAvailable() =
        runTest {
            val testTag = "testMyTag"
            val jsTagEvent: JsTagEvent =
                js("{ type: '$RECOMMENDATION_EVENT_TYPE', recommendationEventType: 'SOME_TYPE', tag: testTag, attributes: { } }").unsafeCast<JsTagEvent>()

            val exception = shouldThrow<IllegalArgumentException> { jsTracking.track(jsTagEvent) }

            exception.cause?.message shouldBe "Invalid recommendation event type: SOME_TYPE. Valid types are: category_view, item_view, search, cart, purchase, tag"
        }
}
