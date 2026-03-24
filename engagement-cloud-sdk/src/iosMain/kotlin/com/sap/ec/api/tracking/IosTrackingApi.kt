package com.sap.ec.api.tracking

import com.sap.ec.api.event.model.TrackedEvent
import io.ktor.utils.io.CancellationException

interface IosTrackingApi {
    @Throws(CancellationException::class)
    suspend fun track(event: TrackedEvent)
}