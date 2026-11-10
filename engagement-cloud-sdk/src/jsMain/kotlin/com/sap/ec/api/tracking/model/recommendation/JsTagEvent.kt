package com.sap.ec.api.tracking.model.recommendation

import com.sap.ec.api.tracking.model.JsRecommendationEvent

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("TagEvent")
interface JsTagEvent : JsRecommendationEvent {

    override val recommendationEventType: String
        get() = JsRecommendationEventType.TAG.name

    val tag: String

    val attributes: dynamic
}