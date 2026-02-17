package com.sap.ec.api.event

import com.sap.ec.api.AutoRegisterable
import com.sap.ec.api.event.model.TrackedEvent

internal interface EventTrackerApi: AutoRegisterable {
    suspend fun trackEvent(trackedEvent: TrackedEvent): Result<Unit>
}