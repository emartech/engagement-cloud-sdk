package com.sap.ec.api.tracking.util

import com.sap.ec.api.tracking.model.JsEventType
import com.sap.ec.api.tracking.model.JsRecommendationEvent
import com.sap.ec.api.tracking.model.recommendation.JSCategoryViewEvent
import com.sap.ec.api.tracking.model.recommendation.JsCartEvent
import com.sap.ec.api.tracking.model.recommendation.JsCartItem
import com.sap.ec.api.tracking.model.recommendation.JsItemViewEvent
import com.sap.ec.api.tracking.model.recommendation.JsPurchaseEvent
import com.sap.ec.api.tracking.model.recommendation.JsRecommendationEventType
import com.sap.ec.api.tracking.model.recommendation.JsSearchEvent
import com.sap.ec.api.tracking.model.recommendation.JsTagEvent

@OptIn(ExperimentalJsExport::class)
@JsExport
fun createItemViewEvent(itemId: String): JsRecommendationEvent {
    return object : JsItemViewEvent {
        override val type = JsEventType.RECOMMENDATION.name
        override val recommendationEventType = JsRecommendationEventType.ITEM_VIEW.name
        override val itemId = itemId
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun createSearchEvent(searchTerm: String): JsRecommendationEvent {
    return object : JsSearchEvent {
        override val type = JsEventType.RECOMMENDATION.name
        override val recommendationEventType = JsRecommendationEventType.SEARCH.name
        override val searchTerm = searchTerm
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun createCategoryViewEvent(categoryPath: String): JsRecommendationEvent {
    return object : JSCategoryViewEvent {
        override val type = JsEventType.RECOMMENDATION.name
        override val recommendationEventType = JsRecommendationEventType.CATEGORY_VIEW.name
        override val categoryPath = categoryPath
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun createTagEvent(tag: String, attributes: dynamic? = null): JsRecommendationEvent {
    return object : JsTagEvent {
        override val type = JsEventType.RECOMMENDATION.name
        override val recommendationEventType = JsRecommendationEventType.TAG.name
        override val tag = tag
        override val attributes = attributes
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun createCartEvent(items: Array<JsCartItem>): JsRecommendationEvent {
    return object : JsCartEvent {
        override val type = JsEventType.RECOMMENDATION.name
        override val recommendationEventType = JsRecommendationEventType.CART.name
        override val items = items
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun createPurchaseEvent(orderId: String, items: Array<JsCartItem>): JsRecommendationEvent {
    return object : JsPurchaseEvent {
        override val type = JsEventType.RECOMMENDATION.name
        override val recommendationEventType = JsRecommendationEventType.PURCHASE.name
        override val orderId = orderId
        override val items = items
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun createCartItem(itemId: String, price: Double, quantity: Double): JsCartItem {
    return object : JsCartItem {
        override val itemId = itemId
        override val price = price
        override val quantity = quantity
    }
}
