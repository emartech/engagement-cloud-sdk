package com.sap.ec.recommendation.networking

import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.RecommendationEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.TagWithAttributes
import com.sap.ec.recommendation.CartItem
import com.sap.ec.recommendation.FilterType
import com.sap.ec.recommendation.RecommendationConstants.AVAILABILITY_ZONE_KEY
import com.sap.ec.recommendation.RecommendationConstants.CART_ITEMS_KEY
import com.sap.ec.recommendation.RecommendationConstants.CART_LIST_ITEM_PRICE_KEY
import com.sap.ec.recommendation.RecommendationConstants.CART_LIST_ITEM_QUANTITY_KEY
import com.sap.ec.recommendation.RecommendationConstants.CART_VERSION_FLAG_KEY
import com.sap.ec.recommendation.RecommendationConstants.CHECKOUT_ITEMS_KEY
import com.sap.ec.recommendation.RecommendationConstants.COHORT_KEY
import com.sap.ec.recommendation.RecommendationConstants.CURRENCY_KEY
import com.sap.ec.recommendation.RecommendationConstants.FEATURE_ID_KEY
import com.sap.ec.recommendation.RecommendationConstants.FEATURE_KEY
import com.sap.ec.recommendation.RecommendationConstants.FEATURES_TO_RETRIEVE_RECOMMENDATIONS_FOR_KEY
import com.sap.ec.recommendation.RecommendationConstants.FILTER_EXCLUDE_KEY
import com.sap.ec.recommendation.RecommendationConstants.FILTER_FIELD_KEY
import com.sap.ec.recommendation.RecommendationConstants.FILTER_NEGATE_KEY
import com.sap.ec.recommendation.RecommendationConstants.FILTER_RULE_KEY
import com.sap.ec.recommendation.RecommendationConstants.FILTER_VALUE_KEY
import com.sap.ec.recommendation.RecommendationConstants.ITEM_ID_KEY
import com.sap.ec.recommendation.RecommendationConstants.ITEM_VIEW_KEY
import com.sap.ec.recommendation.RecommendationConstants.LANGUAGE_KEY
import com.sap.ec.recommendation.RecommendationConstants.LIMIT_KEY
import com.sap.ec.recommendation.RecommendationConstants.OFFSET_KEY
import com.sap.ec.recommendation.RecommendationConstants.ORDER_ID_KEY
import com.sap.ec.recommendation.RecommendationConstants.SEARCH_KEY
import com.sap.ec.recommendation.RecommendationConstants.TAG_KEY
import com.sap.ec.recommendation.RecommendationConstants.TAG_WITH_ATTRIBUTES_KEY
import com.sap.ec.recommendation.RecommendationConstants.VIEW_CATEGORY_KEY
import com.sap.ec.util.JsonUtil
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.buildUrl
import io.ktor.http.encodeURLParameter
import io.ktor.http.takeFrom
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class RecommendationRequestFactory(
    private val urlFactory: UrlFactoryApi,
    private val cartItemStorage: ThreadSafePersistentStoreApi<CartItem>
) : RecommendationRequestFactoryApi {

    override suspend fun create(recommendationEvent: RecommendationEvent): UrlRequest {
        val baseUrlWithAppCode = urlFactory.create(ECUrlType.Recommendation)
        val url = buildUrl {
            takeFrom(baseUrlWithAppCode)
            when (recommendationEvent) {
                is SdkEvent.External.RecommendationTrackEvent -> {
                    appendRecommendationTrackEventParams(recommendationEvent)
                }

                is SdkEvent.Internal.Sdk.RequestRecommendation -> {
                    parameters.append(
                        FEATURES_TO_RETRIEVE_RECOMMENDATIONS_FOR_KEY,
                        "${FEATURE_ID_KEY}:${recommendationEvent.options.logic},${LIMIT_KEY}:${recommendationEvent.options.limit},${OFFSET_KEY}:${recommendationEvent.options.offset}"
                    )
                    recommendationEvent.options.filters?.takeIf { it.isNotEmpty() }
                        ?.let { filters ->
                            parameters.append(FILTER_EXCLUDE_KEY, buildJsonArray {
                                filters.forEach { filter ->
                                    add(buildJsonObject {
                                        put(FILTER_FIELD_KEY, filter.field)
                                        put(FILTER_RULE_KEY, filter.comparison.toString())
                                        put(FILTER_VALUE_KEY, filter.expectations.joinToString("|"))
                                        put(
                                            FILTER_NEGATE_KEY,
                                            filter.type == FilterType.INCLUDE
                                        )
                                    })
                                }
                            }.toString())
                        }
                    recommendationEvent.options.availabilityZone?.takeIf { it.isNotEmpty() }?.let {
                        parameters.append(AVAILABILITY_ZONE_KEY, it)
                    }
                    recommendationEvent.options.language?.takeIf { it.isNotEmpty() }?.let {
                        parameters.append(LANGUAGE_KEY, it)
                    }
                    recommendationEvent.options.displayCurrency?.takeIf { it.isNotEmpty() }?.let {
                        parameters.append(CURRENCY_KEY, it)
                    }
                    recommendationEvent.contextEvent?.let { recommendationTrackEvent ->
                        appendRecommendationTrackEventParams(recommendationTrackEvent)
                    } ?: appendStoredCartItems()
                }
            }
        }

        return UrlRequest(
            url = url,
            method = HttpMethod.Get
        )
    }

    private fun URLBuilder.appendRecommendationTrackEventParams(event: SdkEvent.External.RecommendationTrackEvent) {
        appendEventParams(event)
        appendCartParams(event)
    }

    private fun URLBuilder.appendEventParams(
        event: SdkEvent.External.RecommendationTrackEvent
    ) {
        when (event) {
            is SdkEvent.External.RecommendationTrackEvent.Cart -> {}
            is SdkEvent.External.RecommendationTrackEvent.CategoryView -> {
                parameters.append(
                    VIEW_CATEGORY_KEY,
                    event.categoryPath
                )
            }

            is SdkEvent.External.RecommendationTrackEvent.ItemView -> {
                parameters.append(
                    ITEM_VIEW_KEY,
                    "${ITEM_ID_KEY}:${event.itemId.encodeURLParameter()}"
                )
            }

            is SdkEvent.External.RecommendationTrackEvent.Purchase -> {
                parameters.append(ORDER_ID_KEY, event.orderId)
                parameters.append(
                    CHECKOUT_ITEMS_KEY,
                    event.items.toUrlParamValue()
                )
            }

            is SdkEvent.External.RecommendationTrackEvent.RecommendationTrackClick -> {
                parameters.append(
                    ITEM_VIEW_KEY,
                    "${ITEM_ID_KEY}:${event.productId.encodeURLParameter()},${FEATURE_KEY}:${event.feature},${COHORT_KEY}:${event.cohort}"
                )
            }

            is SdkEvent.External.RecommendationTrackEvent.Search -> {
                parameters.append(
                    SEARCH_KEY,
                    event.searchTerm
                )
            }

            is SdkEvent.External.RecommendationTrackEvent.Tag -> {
                event.attributes?.let {
                    parameters.append(
                        TAG_WITH_ATTRIBUTES_KEY,
                        JsonUtil.json.encodeToString(
                            TagWithAttributes(
                                event.tag,
                                it
                            )
                        )
                    )
                } ?: parameters.append(TAG_KEY, event.tag)
            }
        }
    }

    private fun URLBuilder.appendCartParams(event: SdkEvent.External.RecommendationTrackEvent) {
        when (event) {
            is SdkEvent.External.RecommendationTrackEvent.Cart -> {
                appendCartItems(event.items)
            }

            is SdkEvent.External.RecommendationTrackEvent.Purchase -> {
                appendCartItems(emptyList())
            }

            else -> appendStoredCartItems()
        }
    }

    private fun URLBuilder.appendStoredCartItems() {
        appendCartItems(cartItemStorage.items)
    }

    private fun URLBuilder.appendCartItems(cartItems: List<CartItem>) {
        parameters.append(CART_VERSION_FLAG_KEY, "1")
        parameters.append(
            CART_ITEMS_KEY,
            cartItems.toUrlParamValue()
        )
    }

    private fun List<CartItem>.toUrlParamValue(): String {
        return this.joinToString("|") { item ->
            "${ITEM_ID_KEY}:${item.itemId.encodeURLParameter()},${CART_LIST_ITEM_PRICE_KEY}:${item.price},${CART_LIST_ITEM_QUANTITY_KEY}:${item.quantity}"
        }
    }
}