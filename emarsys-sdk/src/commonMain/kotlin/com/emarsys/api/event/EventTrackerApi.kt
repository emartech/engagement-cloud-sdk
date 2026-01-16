package com.emarsys.api.event

import com.emarsys.api.AutoRegisterable
import com.emarsys.api.event.model.TrackedEvent

internal interface EventTrackerApi: AutoRegisterable {
    suspend fun trackEvent(trackedEvent: TrackedEvent): Result<Unit>
}