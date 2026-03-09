package com.sap.ec.api.event

import com.sap.ec.api.Activatable
import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.api.generic.GenericApi
import com.sap.ec.context.SdkContextApi
import kotlinx.coroutines.withContext

internal interface EventTrackerInstance : EventTrackerInternalApi, Activatable

internal class EventTracker<Logging : EventTrackerInstance, Gatherer : EventTrackerInstance, Internal : EventTrackerInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(
    loggingApi, gathererApi, internalApi, sdkContext
), EventTrackerApi {
    override suspend fun trackEvent(trackedEvent: TrackedEvent): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<EventTrackerInternalApi>().trackEvent(trackedEvent)
        }
    }
}