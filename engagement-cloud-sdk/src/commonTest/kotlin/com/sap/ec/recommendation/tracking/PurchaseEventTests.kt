package com.sap.ec.recommendation.tracking

import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.CartItem
import com.sap.ec.recommendation.models.tracking.PurchaseEvent
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Instant

class PurchaseEventTests {
    private companion object {
        const val TEST_UUID = "testUuid"
        const val ORDER_ID = "testOrderId"
        val TIMESTAMP = Instant.DISTANT_FUTURE
    }

    @Test
    fun toSdkEvent_shouldReturnSuccess_ifItems_isNotEmpty() {
        val testOrderId = "testOrderId"
        val cartItem1 = CartItem("testId", 10.0, 1.0)
        val cartItem2 = CartItem("testId2", 20.0, 3.0)

        val testEvent = PurchaseEvent(testOrderId, listOf(cartItem1, cartItem2))

        val result = testEvent.toSdkEvent(TEST_UUID, TIMESTAMP)

        result.isSuccess shouldBe true

        with(result.getOrNull() as SdkEvent.External.RecommendationTrackEvent.Purchase) {
            orderId shouldBe testOrderId
            items shouldBe listOf(cartItem1, cartItem2)
        }
    }

    @Test
    fun toSdkEvent_shouldReturnFailure_withIllegalArgumentException_ifItems_isEmpty() {
        val testEvent = PurchaseEvent(ORDER_ID, listOf())

        val result = testEvent.toSdkEvent(TEST_UUID, TIMESTAMP)

        result.isSuccess shouldBe false

        with(result.exceptionOrNull() as IllegalArgumentException) {
            message shouldBe "Cart items list cannot be empty!"
        }
    }
}