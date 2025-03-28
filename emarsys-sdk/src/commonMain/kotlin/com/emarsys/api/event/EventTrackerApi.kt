package com.emarsys.api.event

import com.emarsys.api.AutoRegisterable
import com.emarsys.api.event.model.CustomEvent

internal interface EventTrackerApi: AutoRegisterable {
    suspend fun trackEvent(event: CustomEvent): Result<Unit>
}