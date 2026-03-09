package com.sap.ec.api.event

import com.sap.ec.InternalSdkApi
import com.sap.ec.api.event.model.TrackedEvent

@InternalSdkApi
interface EventTrackerInternalApi {

    suspend fun trackEvent(trackedEvent: TrackedEvent)
}