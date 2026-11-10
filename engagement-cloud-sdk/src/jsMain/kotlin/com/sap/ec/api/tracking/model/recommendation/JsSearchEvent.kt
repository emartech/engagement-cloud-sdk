package com.sap.ec.api.tracking.model.recommendation

import com.sap.ec.api.tracking.model.JsRecommendationEvent

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("SearchEvent")
interface JsSearchEvent : JsRecommendationEvent {
    override val recommendationEventType: String
        get() = JsRecommendationEventType.SEARCH.name

    val searchTerm: String
}