package com.emarsys.api.tracking

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.tracking.TrackingApi
import com.emarsys.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

class JSTracking(
    private val trackingApi: TrackingApi,
    private val applicationScope: CoroutineScope
) : JSTrackingApi {

    /**
     * Tracks a custom event with the specified name and optional attributes. These custom events can be used to trigger In-App campaigns or any automation configured at Emarsys.
     *
     * @param eventName The name of the custom event.
     * @param eventPayload Optional payload for the event.
     * @return A promise that resolves when the event is tracked.
     */
    override fun trackCustomEvent(
        eventName: String,
        eventPayload: Any?
    ): Promise<Unit> {
        return applicationScope.promise {
            val attributes: Map<String, String>? = eventPayload?.let {
                 JsonUtil.json.decodeFromString(JSON.stringify(it))
            }
            trackingApi.trackCustomEvent(CustomEvent(eventName, attributes)).getOrThrow()
        }
    }
}