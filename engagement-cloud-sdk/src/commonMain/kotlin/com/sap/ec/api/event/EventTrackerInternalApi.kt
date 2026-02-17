package com.sap.ec.api.event

import com.sap.ec.api.event.model.TrackedEvent

interface EventTrackerInternalApi {

    suspend fun trackEvent(trackedEvent: TrackedEvent)
}