package com.sap.ec.api.tracking

import com.sap.ec.api.tracking.model.JsCustomEvent

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSTrackingApi {

    suspend fun trackEvent(event: JsCustomEvent)

    suspend fun trackNavigation(location: String)
}