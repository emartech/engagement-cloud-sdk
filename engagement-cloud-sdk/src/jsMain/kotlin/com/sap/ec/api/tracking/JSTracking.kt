package com.sap.ec.api.tracking

import com.sap.ec.api.event.model.CustomEvent
import com.sap.ec.api.event.model.NavigateEvent
import com.sap.ec.api.tracking.model.JsCustomEvent
import com.sap.ec.tracking.TrackingApi
import com.sap.ec.util.JsonUtil
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromDynamic

internal class JSTracking(
    private val trackingApi: TrackingApi
) : JSTrackingApi {

    /**
     * Tracks an event.
     * Custom events can be used to trigger In-App campaigns or any automation configured at Engagement Cloud.
     * Navigate events can be used to track page views.
     *
     * @param event The name of the custom event.
     */
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun trackEvent(event: JsCustomEvent) {
        val attributesMap =
            parseAttributesMap(event.attributes)
        trackingApi.track(
            CustomEvent(
                name = event.name,
                attributes = attributesMap.ifEmpty { null }
            )
        ).getOrThrow()
    }


    /**
     * Tracks a navigation.
     *
     * @param location The location that the navigation happens to.
     */
    override suspend fun trackNavigation(location: String) {
        trackingApi.track(NavigateEvent(location = location)).getOrThrow()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun parseAttributesMap(attributes: dynamic): Map<String, String> =
        try {
            JsonUtil.json.decodeFromDynamic<Map<String, String>>(attributes)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse attributes map", e)
        }
}