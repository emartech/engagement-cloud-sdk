package com.sap.ec.event

import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.recommendation.models.CartItem
import com.sap.ec.recommendation.store.RecommendationCartStorageUpdater
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationLogic
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions
import dev.mokkery.MockMode
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RecommendationCartStorageUpdaterTests {

    lateinit var mockCartItemStorage: ThreadSafePersistentStoreApi<CartItem>

    @BeforeTest
    fun setup(){
        mockCartItemStorage = mock(MockMode.autofill)
    }

    @Test
    fun updateFromEvent_shouldSetAllCartItem_toCartStorage_whenEventIsCartEvent() = runTest {
        val cartItems = listOf(
            CartItem("testId", 10.0, 1.0),
            CartItem("testId2", 20.0, 3.0)
        )
        val cartEvent = SdkEvent.External.RecommendationTrackEvent.Cart(cartItems)

        RecommendationCartStorageUpdater(mockCartItemStorage).updateFromEvent(cartEvent)

        verifySuspend { mockCartItemStorage.setAll(cartItems) }
    }

    @Test
    fun updateFromEvent_shouldClearCartStorage_whenEventIsPurchase() = runTest {
        val cartItems = listOf(
            CartItem("testId", 10.0, 1.0),
            CartItem("testId2", 20.0, 3.0)
        )
        val purchaseEvent = SdkEvent.External.RecommendationTrackEvent.Purchase("testOrderId", cartItems)

        RecommendationCartStorageUpdater(mockCartItemStorage).updateFromEvent(purchaseEvent)

        verifySuspend { mockCartItemStorage.clear() }
    }

    @Test
    fun updateFromEvent_shouldNotDoAnything_whenEventIsNotCart_orPurchaseEvent() = runTest {
        val itemViewEvent = SdkEvent.External.RecommendationTrackEvent.ItemView("testItemId")

        RecommendationCartStorageUpdater(mockCartItemStorage).updateFromEvent(itemViewEvent)

        verifySuspend(VerifyMode.exactly(0)) {
            mockCartItemStorage.setAll(any())
            mockCartItemStorage.clear()
        }
    }

    @Test
    fun updateFromEvent_shouldSetAllCartItem_toCartStorage_whenEventIsRequestRecommendation_andHasContextEvent_CartEvent() = runTest {
        val cartItems = listOf(
            CartItem("testId", 10.0, 1.0),
            CartItem("testId2", 20.0, 3.0)
        )
        val cartEvent = SdkEvent.External.RecommendationTrackEvent.Cart(cartItems)
        val requestRecommendationEvent = SdkEvent.Internal.Sdk.RequestRecommendation(
            options = RecommendationOptions(
                logic = RecommendationLogic.Cart
            ),
            contextEvent = cartEvent
        )

        RecommendationCartStorageUpdater(mockCartItemStorage).updateFromEvent(requestRecommendationEvent)

        verifySuspend { mockCartItemStorage.setAll(cartItems) }
    }

    @Test
    fun updateFromEvent_shouldClearCartStorage_whenEventIsRequestRecommendation_andHasContextEvent_Purchase() = runTest {
        val cartItems = listOf(
            CartItem("testId", 10.0, 1.0),
            CartItem("testId2", 20.0, 3.0)
        )
        val purchaseEvent = SdkEvent.External.RecommendationTrackEvent.Purchase("testOrderId", cartItems)
        val requestRecommendationEvent = SdkEvent.Internal.Sdk.RequestRecommendation(
            options = RecommendationOptions(
                logic = RecommendationLogic.Home
            ),
            contextEvent = purchaseEvent
        )

        RecommendationCartStorageUpdater(mockCartItemStorage).updateFromEvent(requestRecommendationEvent)

        verifySuspend { mockCartItemStorage.clear() }
    }

    @Test
    fun updateFromEvent_shouldNotDoAnything_whenEventIsRequestRecommendation_andHasNoContextEvent() = runTest {
        val requestRecommendationEvent = SdkEvent.Internal.Sdk.RequestRecommendation(
            options = RecommendationOptions(
                logic = RecommendationLogic.Home
            ),
            contextEvent = null
        )

        RecommendationCartStorageUpdater(mockCartItemStorage).updateFromEvent(requestRecommendationEvent)

        verifySuspend(VerifyMode.exactly(0)) {
            mockCartItemStorage.setAll(any())
            mockCartItemStorage.clear()
        }
    }
}