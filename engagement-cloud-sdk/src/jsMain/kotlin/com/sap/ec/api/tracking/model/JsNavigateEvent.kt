package com.sap.ec.api.tracking.model


@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("NavigateEvent")
interface JsNavigateEvent : JsTrackedEvent {
    override val type: String
        get() = JsEventType.NAVIGATE.name
    val location: String
}