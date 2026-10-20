package com.sap.ec.api.tracking.util

import com.sap.ec.recommendation.models.tracking.TrackedRecommendationEvent
import com.sap.ec.api.tracking.model.JsCartEventValidationData
import com.sap.ec.api.tracking.model.JsCategoryViewEventValidationData
import com.sap.ec.api.tracking.model.JsItemViewEventValidationData
import com.sap.ec.api.tracking.model.JsPurchaseEventValidationData
import com.sap.ec.api.tracking.model.JsRecommendationEvent
import com.sap.ec.api.tracking.model.JsSearchEventValidationData
import com.sap.ec.api.tracking.model.JsTagEventValidationData
import com.sap.ec.api.tracking.model.recommendation.JSCategoryViewEvent
import com.sap.ec.api.tracking.model.recommendation.JsCartEvent
import com.sap.ec.api.tracking.model.recommendation.JsCartItem
import com.sap.ec.api.tracking.model.recommendation.JsItemViewEvent
import com.sap.ec.api.tracking.model.recommendation.JsPurchaseEvent
import com.sap.ec.api.tracking.model.recommendation.JsRecommendationEventType
import com.sap.ec.api.tracking.model.recommendation.JsSearchEvent
import com.sap.ec.api.tracking.model.recommendation.JsTagEvent
import com.sap.ec.recommendation.models.CartItem
import com.sap.ec.recommendation.models.tracking.CartEvent
import com.sap.ec.recommendation.models.tracking.CategoryViewEvent
import com.sap.ec.recommendation.models.tracking.ItemViewEvent
import com.sap.ec.recommendation.models.tracking.PurchaseEvent
import com.sap.ec.recommendation.models.tracking.SearchEvent
import com.sap.ec.recommendation.models.tracking.TagEvent


private val possibleRecommendationEventTypes =
    JsRecommendationEventType.entries.map { it.toString() }
private val validRecommendationEventTypesString =
    possibleRecommendationEventTypes.joinToString(", ") { it.lowercase() }

@OptIn(ExperimentalWasmJsInterop::class)
internal fun parseRecommendationEvent(event: JsRecommendationEvent): TrackedRecommendationEvent {
    if (!possibleRecommendationEventTypes.contains(event.recommendationEventType)) {
        throw IllegalArgumentException(
            "Invalid recommendation event type: ${event.recommendationEventType}. Valid types are: $validRecommendationEventTypesString"
        )
    }
    return when (JsRecommendationEventType.valueOf(event.recommendationEventType)) {
        JsRecommendationEventType.CATEGORY_VIEW -> {
            val event =
                event.parseWithValidation<JsCategoryViewEventValidationData, JSCategoryViewEvent>()
            CategoryViewEvent(event.categoryPath)
        }

        JsRecommendationEventType.ITEM_VIEW -> {
            val event =
                event.parseWithValidation<JsItemViewEventValidationData, JsItemViewEvent>()
            ItemViewEvent(event.itemId)
        }

        JsRecommendationEventType.SEARCH -> {
            val event =
                event.parseWithValidation<JsSearchEventValidationData, JsSearchEvent>()
            SearchEvent(event.searchTerm)
        }

        JsRecommendationEventType.CART -> {
            val event = event.parseWithValidation<JsCartEventValidationData, JsCartEvent>()
            CartEvent(event.items.toCartItems())
        }

        JsRecommendationEventType.PURCHASE -> {
            val event =
                event.parseWithValidation<JsPurchaseEventValidationData, JsPurchaseEvent>()
            if (event.items.isEmpty()) {
                throw IllegalArgumentException("Cart items list cannot be empty!")
            }
            PurchaseEvent(event.orderId, event.items.toCartItems())
        }

        JsRecommendationEventType.TAG -> {
            val event = event.parseWithValidation<JsTagEventValidationData, JsTagEvent>()
            TagEvent(
                tag = event.tag,
                attributes = parseAttributesMap(event.attributes).ifEmpty { null })
        }
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun Array<JsCartItem>.toCartItems(): List<CartItem> {
    return this.map { jsCartItem ->
        CartItem(
            itemId = jsCartItem.itemId,
            price = jsCartItem.price,
            quantity = jsCartItem.quantity
        )
    }
}

