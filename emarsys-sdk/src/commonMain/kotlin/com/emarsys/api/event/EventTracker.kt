package com.emarsys.api.event

import Activatable
import EventTrackerApi
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.generic.GenericApi
import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.withContext

interface EventTrackerInstance : EventTrackerInternalApi, Activatable

class EventTracker<Logging : EventTrackerInstance, Gatherer : EventTrackerInstance, Internal : EventTrackerInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(
    loggingApi, gathererApi, internalApi, sdkContext
), EventTrackerApi {
    override suspend fun trackEvent(event: CustomEvent): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<EventTrackerInternalApi>().trackEvent(event)
        }
    }
}