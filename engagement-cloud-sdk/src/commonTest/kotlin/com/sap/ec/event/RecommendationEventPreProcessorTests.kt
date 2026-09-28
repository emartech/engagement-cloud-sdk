package com.sap.ec.event

import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.recommendation.CartItem
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RecommendationEventPreProcessorTests {

    lateinit var mockCartItemStorage: ThreadSafePersistentStoreApi<CartItem>

    @BeforeTest
    fun setup(){
        mockCartItemStorage = mock(MockMode.autofill)
    }

    @Test
    fun process_shouldAddCartItems_toCartStorage_whenEventIsCartEvent() = runTest {
        val cartItems = listOf(
            CartItem("testId", 10.0, 1.0),
            CartItem("testId2", 20.0, 3.0)
        )
        val event = SdkEvent.External.RecommendationTrackEvent.Cart(cartItems)

        RecommendationEventPreProcessor(mockCartItemStorage).process(event)

        verifySuspend { mockCartItemStorage.setAll(cartItems) }
    }
}