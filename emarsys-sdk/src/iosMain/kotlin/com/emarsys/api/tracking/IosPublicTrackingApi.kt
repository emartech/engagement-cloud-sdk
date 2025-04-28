package com.emarsys.api.tracking

import com.emarsys.api.event.model.CustomEvent
import io.ktor.utils.io.CancellationException

interface IosPublicTrackingApi {
    @Throws(CancellationException::class)
    suspend fun track(event: CustomEvent)
}