package com.sap.ec.api.tracking.util

import com.sap.ec.api.tracking.model.JsEventType
import com.sap.ec.api.tracking.model.recommendation.JSCategoryViewEvent
import com.sap.ec.api.tracking.model.recommendation.JsCartEvent
import com.sap.ec.api.tracking.model.recommendation.JsItemViewEvent
import com.sap.ec.api.tracking.model.recommendation.JsPurchaseEvent
import com.sap.ec.api.tracking.model.recommendation.JsRecommendationEventType
import com.sap.ec.api.tracking.model.recommendation.JsSearchEvent
import com.sap.ec.api.tracking.model.recommendation.JsTagEvent
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class JsRecommendationEventFactoryTests {
    private companion object {
        const val TEST_ID = "testId"
        const val TEST_PRICE = 123.456
        const val TEST_QUANTITY = 12.toDouble()
        const val TEST_ID_2 = "testId2"
        const val TEST_PRICE_2 = 2.46
        const val TEST_QUANTITY_2 = 33.toDouble()
    }

    @Test
    fun createItemViewEvent_shouldReturn_jsItemViewEvent() {
        val testItemId = "testItemId"

        val event = createItemViewEvent(testItemId)

        with(event as JsItemViewEvent) {
            type shouldBe JsEventType.RECOMMENDATION.name
            recommendationEventType shouldBe JsRecommendationEventType.ITEM_VIEW.name
            itemId shouldBe testItemId
        }
    }

    @Test
    fun createSearchEvent_shouldReturn_jsSearchEvent() {
        val testSearchTerm = "testSearchTerm"

        val event = createSearchEvent(testSearchTerm)

        with(event as JsSearchEvent) {
            type shouldBe JsEventType.RECOMMENDATION.name
            recommendationEventType shouldBe JsRecommendationEventType.SEARCH.name
            searchTerm shouldBe testSearchTerm
        }
    }

    @Test
    fun createCategoryViewEvent_shouldReturn_jsCategoryViewEvent() {
        val testCategory = "testCategory"

        val event = createCategoryViewEvent(testCategory)

        with(event as JSCategoryViewEvent) {
            type shouldBe JsEventType.RECOMMENDATION.name
            recommendationEventType shouldBe JsRecommendationEventType.CATEGORY_VIEW.name
            categoryPath shouldBe testCategory
        }
    }

    @Test
    fun createTagEvent_shouldReturn_jsTagEvent() {
        val testTag = "testTag"

        val event = createTagEvent(testTag)

        with(event as JsTagEvent) {
            type shouldBe JsEventType.RECOMMENDATION.name
            recommendationEventType shouldBe JsRecommendationEventType.TAG.name
            tag shouldBe testTag
        }
    }

    @Test
    fun createTagEvent_shouldReturn_jsTagEvent_withAttributes() {
        val testTag = "testTag"
        val testAttributes = "{ 'key': 'value' }"

        val event = createTagEvent(testTag, testAttributes)

        with(event as JsTagEvent) {
            type shouldBe JsEventType.RECOMMENDATION.name
            recommendationEventType shouldBe JsRecommendationEventType.TAG.name
            tag shouldBe testTag
            attributes.unsafeCast<String>() shouldBe testAttributes
        }
    }

    @Test
    fun createCartItem_shouldReturn_jsCartItem() {
        val cartItem = createCartItem(TEST_ID, TEST_PRICE, TEST_QUANTITY)

        with(cartItem) {
            itemId shouldBe TEST_ID
            price shouldBe TEST_PRICE
            quantity shouldBe TEST_QUANTITY
        }
    }

    @Test
    fun createCartEvent_shouldReturn_jsCartEvent() {
        val cartItem = createCartItem(TEST_ID, TEST_PRICE, TEST_QUANTITY)
        val cartItem2 = createCartItem(TEST_ID_2, TEST_PRICE_2, TEST_QUANTITY_2)

        val event = createCartEvent(arrayOf(cartItem, cartItem2))

        with(event as JsCartEvent) {
            type shouldBe JsEventType.RECOMMENDATION.name
            recommendationEventType shouldBe JsRecommendationEventType.CART.name
            items shouldBe arrayOf(cartItem, cartItem2)
        }
    }

    @Test
    fun createCartEvent_shouldReturn_jsCartEvent_emptyCart() {
        val event = createCartEvent(arrayOf())

        with(event as JsCartEvent) {
            type shouldBe JsEventType.RECOMMENDATION.name
            recommendationEventType shouldBe JsRecommendationEventType.CART.name
            items shouldBe arrayOf()
        }
    }

    @Test
    fun createPurchaseEvent_shouldReturn_jsPurchaseEvent() {
        val testOrderId = "testOrderId"
        val cartItem = createCartItem(TEST_ID, TEST_PRICE, TEST_QUANTITY)
        val cartItem2 = createCartItem(TEST_ID_2, TEST_PRICE_2, TEST_QUANTITY_2)

        val event = createPurchaseEvent(testOrderId, arrayOf(cartItem, cartItem2))

        with(event as JsPurchaseEvent) {
            type shouldBe JsEventType.RECOMMENDATION.name
            recommendationEventType shouldBe JsRecommendationEventType.PURCHASE.name
            orderId shouldBe testOrderId
            items shouldBe arrayOf(cartItem, cartItem2)
        }
    }

    @Test
    fun createPurchaseEvent_shouldReturn_jsPurchaseEvent_emptyCart() {
        val testOrderId = "testOrderId"

        val event = createPurchaseEvent(testOrderId, arrayOf())

        with(event as JsPurchaseEvent) {
            type shouldBe JsEventType.RECOMMENDATION.name
            recommendationEventType shouldBe JsRecommendationEventType.PURCHASE.name
            orderId shouldBe testOrderId
            items shouldBe arrayOf()
        }
    }
}