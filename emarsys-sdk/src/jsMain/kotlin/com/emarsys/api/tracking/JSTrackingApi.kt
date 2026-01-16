package com.emarsys.api.tracking

import com.emarsys.api.tracking.model.JsCustomEvent
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSTrackingApi {

    fun trackEvent(event: JsCustomEvent): Promise<Unit>

    fun trackNavigation(location: String): Promise<Unit>
}