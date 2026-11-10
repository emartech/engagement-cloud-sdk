package com.sap.ec.api.tracking.model.recommendation

import com.sap.ec.api.tracking.model.JsRecommendationEvent

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("PurchaseEvent")
interface JsPurchaseEvent : JsRecommendationEvent {

    override val recommendationEventType: String
        get() = JsRecommendationEventType.PURCHASE.name

    val orderId: String

    val items: Array<JsCartItem>
}