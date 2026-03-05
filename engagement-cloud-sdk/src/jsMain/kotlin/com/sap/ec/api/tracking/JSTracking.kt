package com.sap.ec.api.tracking

import com.sap.ec.api.event.model.CustomEvent
import com.sap.ec.api.event.model.NavigateEvent
import com.sap.ec.api.tracking.model.JsCustomEvent
import com.sap.ec.api.tracking.model.JsCustomEventValidationData
import com.sap.ec.api.tracking.model.JsTrackedEvent
import com.sap.ec.api.tracking.model.JsEventType
import com.sap.ec.api.tracking.model.JsEventValidationData
import com.sap.ec.api.tracking.model.JsNavigateEvent
import com.sap.ec.api.tracking.model.JsNavigateEventValidationData
import com.sap.ec.core.log.Logger
import com.sap.ec.tracking.TrackingApi
import com.sap.ec.util.JsonUtil
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromDynamic

internal class JSTracking(
    private val trackingApi: TrackingApi,
    private val sdkLogger: Logger
) : JSTrackingApi {

    private val possibleEventTypes = JsEventType.entries.map { it.toString() }

    /**
     * Tracks an event.
     * Custom events can be used to trigger In-App campaigns or any automation configured at Engagement Cloud.
     * Navigate events can be used to track page views.
     *
     * @param event The event to track of type [JsTrackedEvent].
     */
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun track(event: JsTrackedEvent) {
        println(possibleEventTypes)
        if (!possibleEventTypes.contains(event.type)) {
            throw IllegalArgumentException("Invalid event type: ${event.type}")
        }
        val event = try {
            when (JsEventType.valueOf(event.type)) {
                JsEventType.CUSTOM -> {
                    validateJsEvent<JsCustomEventValidationData>(event)
                    val jsCustomEvent = event.unsafeCast<JsCustomEvent>()
                    require(jsCustomEvent.attributes != null)
                    val attributesMap =
                        parseAttributesMap(jsCustomEvent.attributes)
                    CustomEvent(
                        name = jsCustomEvent.name,
                        attributes = attributesMap.ifEmpty { null }
                    )
                }

                JsEventType.NAVIGATE -> {
                    validateJsEvent<JsNavigateEventValidationData>(event)
                    val jsNavigateEvent = event.unsafeCast<JsNavigateEvent>()
                    NavigateEvent(location = jsNavigateEvent.location)
                }
            }
        } catch (e: Exception) {
            sdkLogger.debug("Failed to parse event.", e, isRemoteLog = false)
            throw IllegalArgumentException("Failed to parse event.", e)
        }

        trackingApi.track(event).getOrThrow()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun parseAttributesMap(attributes: dynamic): Map<String, String> =
        try {
            JsonUtil.json.decodeFromDynamic<Map<String, String>>(attributes)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse attributes map.", e)
        }

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T : JsEventValidationData> validateJsEvent(event: JsTrackedEvent) {
        JsonUtil.json.decodeFromDynamic<T>(event)
    }

}