package com.sap.ec.api.tracking.model.recommendation

import com.sap.ec.api.tracking.model.JsRecommendationEvent


@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("ItemViewEvent")
interface JsItemViewEvent : JsRecommendationEvent {

    override val recommendationEventType: String
        get() = JsRecommendationEventType.ITEM_VIEW.name

    val itemId: String
}