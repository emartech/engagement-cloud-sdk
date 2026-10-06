package com.sap.ec.api.tracking.model.recommendation

import com.sap.ec.api.tracking.model.JsRecommendationEvent

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("CategoryViewEvent")
interface JSCategoryViewEvent: JsRecommendationEvent {

    override val recommendationEventType: String
        get() = JsRecommendationEventType.CATEGORY_VIEW.name

    val categoryPath: String
}