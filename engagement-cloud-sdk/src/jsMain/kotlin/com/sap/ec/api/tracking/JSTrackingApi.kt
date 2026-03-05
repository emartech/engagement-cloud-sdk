package com.sap.ec.api.tracking

import com.sap.ec.api.tracking.model.JsTrackedEvent

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSTrackingApi {

    suspend fun track(event: JsTrackedEvent)

}