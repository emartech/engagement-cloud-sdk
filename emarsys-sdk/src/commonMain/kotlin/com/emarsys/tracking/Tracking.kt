package com.emarsys.tracking

import com.emarsys.api.event.EventTrackerApi
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.di.SdkKoinIsolationContext.koin

class Tracking : TrackingApi {
    /**
     * Tracks a custom event with the specified name and optional attributes. These custom events can be used to trigger In-App campaigns or any automation configured at Emarsys.
     *
     * @param event custom event to be tracked.
     */
    override suspend fun track(event: CustomEvent): Result<Unit> {
        return koin.get<EventTrackerApi>().trackEvent(event)
    }
}