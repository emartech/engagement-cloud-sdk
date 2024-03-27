package com.emarsys.api.event

import com.emarsys.api.SdkResult
import com.emarsys.api.event.model.CustomEvent

interface EventTrackerInternalApi {

    suspend fun trackEvent(event: CustomEvent): SdkResult
}