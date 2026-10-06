package com.sap.ec.api.tracking.model.recommendation

import com.sap.ec.api.tracking.model.JsRecommendationEvent

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("CartEvent")
interface JsCartEvent : JsRecommendationEvent {

    override val recommendationEventType: String
        get() = JsRecommendationEventType.CART.name

    val items: List<JsCartItem>
}