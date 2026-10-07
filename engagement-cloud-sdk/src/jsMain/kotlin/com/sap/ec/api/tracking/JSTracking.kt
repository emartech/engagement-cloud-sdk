package com.sap.ec.api.tracking

import com.sap.ec.api.event.model.CustomEvent
import com.sap.ec.api.event.model.NavigateEvent
import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.api.tracking.model.JsCartEventValidationData
import com.sap.ec.api.tracking.model.JsCategoryViewEventValidationData
import com.sap.ec.api.tracking.model.JsCustomEvent
import com.sap.ec.api.tracking.model.JsCustomEventValidationData
import com.sap.ec.api.tracking.model.JsEventType
import com.sap.ec.api.tracking.model.JsEventValidationData
import com.sap.ec.api.tracking.model.JsItemViewEventValidationData
import com.sap.ec.api.tracking.model.JsNavigateEvent
import com.sap.ec.api.tracking.model.JsNavigateEventValidationData
import com.sap.ec.api.tracking.model.JsPurchaseEventValidationData
import com.sap.ec.api.tracking.model.JsRecommendationClickEventValidationData
import com.sap.ec.api.tracking.model.JsRecommendationEvent
import com.sap.ec.api.tracking.model.JsRecommendationEventValidationData
import com.sap.ec.api.tracking.model.JsSearchEventValidationData
import com.sap.ec.api.tracking.model.JsTagEventValidationData
import com.sap.ec.api.tracking.model.JsTrackedEvent
import com.sap.ec.api.tracking.model.recommendation.JSCategoryViewEvent
import com.sap.ec.api.tracking.model.recommendation.JsCartEvent
import com.sap.ec.api.tracking.model.recommendation.JsCartItem
import com.sap.ec.api.tracking.model.recommendation.JsItemViewEvent
import com.sap.ec.api.tracking.model.recommendation.JsPurchaseEvent
import com.sap.ec.api.tracking.model.recommendation.JsRecommendationClickEvent
import com.sap.ec.api.tracking.model.recommendation.JsRecommendationEventType
import com.sap.ec.api.tracking.model.recommendation.JsSearchEvent
import com.sap.ec.api.tracking.model.recommendation.JsTagEvent
import com.sap.ec.core.log.Logger
import com.sap.ec.recommendation.models.CartItem
import com.sap.ec.recommendation.models.tracking.CartEvent
import com.sap.ec.recommendation.models.tracking.CategoryViewEvent
import com.sap.ec.recommendation.models.tracking.ItemViewEvent
import com.sap.ec.recommendation.models.tracking.PurchaseEvent
import com.sap.ec.recommendation.models.tracking.RecommendationClickEvent
import com.sap.ec.recommendation.models.tracking.SearchEvent
import com.sap.ec.recommendation.models.tracking.TagEvent
import com.sap.ec.tracking.TrackingApi
import com.sap.ec.util.JsonUtil
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromDynamic

internal class JSTracking(
    private val trackingApi: TrackingApi,
    private val sdkLogger: Logger
) : JSTrackingApi {

    private val possibleEventTypes = JsEventType.entries.map { it.toString() }
    private val possibleRecommendationEventTypes =
        JsRecommendationEventType.entries.map { it.toString() }

    private val validEventTypesString = possibleEventTypes.joinToString(", ") { it.lowercase() }
    private val validRecommendationEventTypesString =
        possibleRecommendationEventTypes.joinToString(", ") { it.lowercase() }

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

    @OptIn(ExperimentalWasmJsInterop::class)
    private fun parseRecommendationEvent(event: JsRecommendationEvent): TrackedEvent {
        if (!possibleRecommendationEventTypes.contains(event.recommendationEventType)) {
            throw IllegalArgumentException(
                "Invalid recommendation event type: ${event.recommendationEventType}. Valid types are: $validRecommendationEventTypesString"
            )
        }
        return when (JsRecommendationEventType.valueOf(event.recommendationEventType)) {
            JsRecommendationEventType.CATEGORY_VIEW -> {
                val event =
                    event.parseWithValidation<JsCategoryViewEventValidationData, JSCategoryViewEvent>()
                CategoryViewEvent(event.categoryPath)
            }

            JsRecommendationEventType.ITEM_VIEW -> {
                val event =
                    event.parseWithValidation<JsItemViewEventValidationData, JsItemViewEvent>()
                ItemViewEvent(event.itemId)
            }

            JsRecommendationEventType.SEARCH -> {
                val event =
                    event.parseWithValidation<JsSearchEventValidationData, JsSearchEvent>()
                SearchEvent(event.searchTerm)
            }

            JsRecommendationEventType.RECOMMENDATION_CLICK -> {
                val event =
                    event.parseWithValidation<JsRecommendationClickEventValidationData, JsRecommendationClickEvent>()
                RecommendationClickEvent(event.productId)
            }

            JsRecommendationEventType.CART -> {
                val event = event.parseWithValidation<JsCartEventValidationData, JsCartEvent>()
                CartEvent(event.items.toCartItems())
            }

            JsRecommendationEventType.PURCHASE -> {
                val event =
                    event.parseWithValidation<JsPurchaseEventValidationData, JsPurchaseEvent>()
                PurchaseEvent(event.orderId, event.items.toCartItems())
            }

            JsRecommendationEventType.TAG -> {
                val event = event.parseWithValidation<JsTagEventValidationData, JsTagEvent>()
                TagEvent(
                    tag = event.tag,
                    attributes = parseAttributesMap(event.attributes).ifEmpty { null })
            }
        }
    }

    private inline fun <reified ValidationData : JsEventValidationData, JSTrackedEventType : JsTrackedEvent> JsTrackedEvent.parseWithValidation(): JSTrackedEventType {
        validateJsEvent<ValidationData>(this)
        return this.unsafeCast<JSTrackedEventType>()
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    private fun Array<JsCartItem>.toCartItems(): List<CartItem> {
        return this.map { jsCartItem ->
            CartItem(
                itemId = jsCartItem.itemId,
                price = jsCartItem.price,
                quantity = jsCartItem.quantity
            )
        }
    }
}