package com.emarsys.tracking

import com.emarsys.api.event.model.CustomEvent

interface TrackingApi {
    suspend fun trackCustomEvent(event: CustomEvent): Result<Unit>
}