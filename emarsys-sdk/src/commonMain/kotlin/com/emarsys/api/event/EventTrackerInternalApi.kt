package com.emarsys.api.event

import com.emarsys.api.event.model.TrackedEvent

interface EventTrackerInternalApi {

    suspend fun trackEvent(trackedEvent: TrackedEvent)
}