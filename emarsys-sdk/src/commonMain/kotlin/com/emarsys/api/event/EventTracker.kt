package com.emarsys.api.event

import Activatable
import com.emarsys.api.event.model.TrackedEvent
import com.emarsys.api.generic.GenericApi
import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.withContext

interface EventTrackerInstance : EventTrackerInternalApi, Activatable

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