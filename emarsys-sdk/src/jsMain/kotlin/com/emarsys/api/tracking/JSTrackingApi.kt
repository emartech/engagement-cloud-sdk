package com.emarsys.api.tracking

import com.emarsys.api.tracking.model.JsCustomEvent

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSTrackingApi {

    suspend fun trackEvent(event: JsCustomEvent)

    suspend fun trackNavigation(location: String)
}