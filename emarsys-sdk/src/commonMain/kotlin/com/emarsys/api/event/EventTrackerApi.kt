package com.emarsys.api.event

interface EventTrackerApi {

    suspend fun trackEvent(event: CustomEvent)
}