package com.sap.ec.api.tracking

import com.sap.ec.api.event.model.CustomEvent
import com.sap.ec.api.event.model.NavigateEvent
import com.sap.ec.api.tracking.model.JsCustomEvent
import com.sap.ec.api.tracking.model.JsCustomEventValidationData
import com.sap.ec.api.tracking.model.JsEventType
import com.sap.ec.api.tracking.model.JsNavigateEvent
import com.sap.ec.api.tracking.model.JsNavigateEventValidationData
import com.sap.ec.api.tracking.model.JsRecommendationEvent
import com.sap.ec.api.tracking.model.JsRecommendationEventValidationData
import com.sap.ec.api.tracking.model.JsTrackedEvent
import com.sap.ec.api.tracking.util.parseAttributesMap
import com.sap.ec.api.tracking.util.parseRecommendationEvent
import com.sap.ec.api.tracking.util.parseWithValidation
import com.sap.ec.core.log.Logger
import com.sap.ec.tracking.TrackingApi
import kotlinx.serialization.ExperimentalSerializationApi

internal class JSTracking(
    private val trackingApi: TrackingApi,
    private val sdkLogger: Logger
) : JSTrackingApi {
    private val possibleEventTypes = JsEventType.entries.map { it.toString() }
    private val validEventTypesString = possibleEventTypes.joinToString(", ") { it.lowercase() }

    /**
     * Tracks an event.
     * Custom events can be used to trigger In-App campaigns or any automation configured at Engagement Cloud.
     * Navigate events can be used to track page views.
     *
     * @param event The event to track of type [JsTrackedEvent].
     */
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun track(event: JsTrackedEvent) {
        val eventType = event.type.uppercase()
        if (!possibleEventTypes.contains(eventType)) {
            throw IllegalArgumentException(
                "Invalid event type: ${event.type}. Valid types are: $validEventTypesString"
            )
        }
        val event = try {
            when (JsEventType.valueOf(eventType)) {
                JsEventType.CUSTOM -> {
                    val jsCustomEvent =
                        event.parseWithValidation<JsCustomEventValidationData, JsCustomEvent>()
                    val attributesMap =
                        parseAttributesMap(jsCustomEvent.attributes)
                    CustomEvent(
                        name = jsCustomEvent.name,
                        attributes = attributesMap.ifEmpty { null }
                    )
                }

                JsEventType.NAVIGATE -> {
                    val jsNavigateEvent =
                        event.parseWithValidation<JsNavigateEventValidationData, JsNavigateEvent>()
                    NavigateEvent(location = jsNavigateEvent.location)
                }

                JsEventType.RECOMMENDATION -> {
                    val jsRecommendationEvent =
                        event.parseWithValidation<JsRecommendationEventValidationData, JsRecommendationEvent>()
                    parseRecommendationEvent(jsRecommendationEvent)
                }
            }
        } catch (e: Exception) {
            sdkLogger.debug("Failed to parse event.", e, isRemoteLog = false)
            throw IllegalArgumentException("Failed to parse event.", e)
        }

        trackingApi.track(event).getOrThrow()
    }
}