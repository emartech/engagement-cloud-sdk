package com.sap.ec.api.tracking

import com.sap.ec.api.event.model.CustomEvent
import io.ktor.utils.io.CancellationException

interface IosTrackingApi {
    @Throws(CancellationException::class)
    suspend fun track(event: CustomEvent)
}