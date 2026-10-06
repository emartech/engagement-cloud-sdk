package com.sap.ec.api.tracking.model

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("RecommendationEvent")
interface JsRecommendationEvent: JsTrackedEvent {

    override val type: String
        get() = JsEventType.RECOMMENDATION.name

    val recommendationEventType: String
}