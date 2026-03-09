package com.sap.ec.api.event

import com.sap.ec.api.event.model.TrackedEvent

internal interface EventTrackerInternalApi {

    suspend fun trackEvent(trackedEvent: TrackedEvent)
}