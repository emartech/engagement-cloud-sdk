package com.emarsys.api.tracking

import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSTrackingApi {
    fun trackCustomEvent(eventName: String, eventPayload: Any?): Promise<Unit>
}