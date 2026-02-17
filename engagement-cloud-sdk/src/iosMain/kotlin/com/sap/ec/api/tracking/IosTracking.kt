package com.sap.ec.api.tracking

import com.sap.ec.api.event.model.CustomEvent
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.tracking.TrackingApi

class IosTracking: IosTrackingApi {
    override suspend fun track(event: CustomEvent) {
        koin.get<TrackingApi>().track(event)
    }
}