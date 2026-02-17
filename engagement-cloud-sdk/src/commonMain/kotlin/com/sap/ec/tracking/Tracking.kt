package com.sap.ec.tracking

import com.sap.ec.api.event.EventTrackerApi
import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.di.SdkKoinIsolationContext.koin

internal class Tracking : TrackingApi {
    /**
     * Tracks a custom event with the specified name and optional attributes. These custom events can be used to trigger In-App campaigns or any automation configured at Engagement Cloud.
     *
     * @param trackedEvent custom event to be tracked.
     */
    override suspend fun track(trackedEvent: TrackedEvent): Result<Unit> {
        return koin.get<EventTrackerApi>().trackEvent(trackedEvent)
    }
}