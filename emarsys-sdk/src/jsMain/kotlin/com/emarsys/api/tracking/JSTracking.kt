package com.emarsys.api.tracking

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.event.model.NavigateEvent
import com.emarsys.api.tracking.model.JsCustomEvent
import com.emarsys.tracking.TrackingApi
import com.emarsys.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Promise

internal class JSTracking(
    private val trackingApi: TrackingApi,
    private val applicationScope: CoroutineScope
) : JSTrackingApi {

    /**
     * Tracks an event.
     * Custom events can be used to trigger In-App campaigns or any automation configured at Emarsys.
     * Navigate events can be used to track page views.
     *
     * @param event The name of the custom event.
     * @return A promise that resolves when the event is tracked.
     */
    @OptIn(ExperimentalSerializationApi::class)
    override fun trackEvent(event: JsCustomEvent): Promise<Unit> {
        return applicationScope.promise {
            val attributesMap =
                parseAttributesMap(event.attributes)
            trackingApi.track(
                CustomEvent(
                    name = event.name,
                    attributes = attributesMap.ifEmpty { null }
                )).getOrThrow()
        }
    }


    /**
     * Tracks a navigation.
     *
     * @param location The location that the navigation happens to.
     * @return A promise that resolves when the navigation is tracked.
     */
    override fun trackNavigation(location: String): Promise<Unit> {
        return applicationScope.promise {
            trackingApi.track(NavigateEvent(location = location)).getOrThrow()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun parseAttributesMap(attributes: dynamic): Map<String, String> =
        try {
            JsonUtil.json.decodeFromDynamic<Map<String, String>>(attributes)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse attributes map", e)
        }
}