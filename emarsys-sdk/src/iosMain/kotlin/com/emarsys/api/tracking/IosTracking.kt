package com.emarsys.api.tracking

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.tracking.TrackingApi

class IosTracking: IosTrackingApi {
    override suspend fun track(event: CustomEvent) {
        koin.get<TrackingApi>().track(event)
    }
}