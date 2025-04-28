package com.emarsys.api.tracking

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.tracking.TrackingApi

class IosPublicTracking: IosPublicTrackingApi {
    override suspend fun trackCustomEvent(event: CustomEvent) {
        koin.get<TrackingApi>().trackCustomEvent(event)
    }
}