package com.sap.ec.mobileengage.recommendation.networking

import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.CART_ITEMS_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.CART_LIST_ITEM_PRICE_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.CART_LIST_ITEM_QUANTITY_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.CART_VERSION_FLAG_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.CHECKOUT_ITEMS_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.COHORT_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.FEATURE_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.ITEM_ID_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.ITEM_VIEW_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.ORDER_ID_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.SEARCH_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.TAG_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.TAG_WITH_ATTRIBUTES_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.VIEW_CATEGORY_KEY
import com.sap.ec.mobileengage.recommendation.models.TagWithAttributes
import com.sap.ec.recommendation.CartItem
import com.sap.ec.util.JsonUtil
import io.ktor.http.HttpMethod
import io.ktor.http.buildUrl
import io.ktor.http.encodeURLParameter
import io.ktor.http.takeFrom

internal class RecommendationRequestFactory(
    private val urlFactory: UrlFactoryApi
) : RecommendationRequestFactoryApi {

    override suspend fun create(recommendationEvent: SdkEvent.External.RecommendationEvent): UrlRequest {
        val baseUrlWithAppCode = urlFactory.create(ECUrlType.Recommendation)
        val url = buildUrl {
            takeFrom(baseUrlWithAppCode)
            when (recommendationEvent) {
                is SdkEvent.External.RecommendationEvent.Cart -> {
                    parameters.append(CART_VERSION_FLAG_KEY, "1")
                    parameters.append(
                        CART_ITEMS_KEY,
                        recommendationEvent.items.toUrlParamValue()
                    )
                }

                is SdkEvent.External.RecommendationEvent.CategoryView -> parameters.append(
                    VIEW_CATEGORY_KEY,
                    recommendationEvent.categoryPath
                )

                is SdkEvent.External.RecommendationEvent.ItemView -> parameters.append(
                    ITEM_VIEW_KEY,
                    "$ITEM_ID_KEY:${recommendationEvent.itemId.encodeURLParameter()}"
                )

                is SdkEvent.External.RecommendationEvent.Purchase -> {
                    parameters.append(ORDER_ID_KEY, recommendationEvent.orderId)
                    parameters.append(
                        CHECKOUT_ITEMS_KEY,
                        recommendationEvent.items.toUrlParamValue()
                    )
                }

                is SdkEvent.External.RecommendationEvent.RecommendationClick -> {
                    parameters.append(
                        ITEM_VIEW_KEY,
                        "$ITEM_ID_KEY:${recommendationEvent.productId.encodeURLParameter()},$FEATURE_KEY:${recommendationEvent.feature},$COHORT_KEY:${recommendationEvent.cohort}"
                    )
                }

                is SdkEvent.External.RecommendationEvent.Search -> parameters.append(
                    SEARCH_KEY,
                    recommendationEvent.searchTerm
                )

                is SdkEvent.External.RecommendationEvent.Tag -> {
                    recommendationEvent.attributes?.let {
                        parameters.append(
                            TAG_WITH_ATTRIBUTES_KEY,
                            JsonUtil.json.encodeToString(
                                TagWithAttributes(
                                    recommendationEvent.tag,
                                    recommendationEvent.attributes
                                )
                            )
                        )
                    } ?: parameters.append(TAG_KEY, recommendationEvent.tag)
                }
            }
        }

        return UrlRequest(
            url = url,
            method = HttpMethod.Get
        )
    }

    private fun List<CartItem>.toUrlParamValue(): String {
        return this.joinToString("|") { item ->
            "$ITEM_ID_KEY:${item.itemId.encodeURLParameter()},$CART_LIST_ITEM_PRICE_KEY:${item.price},$CART_LIST_ITEM_QUANTITY_KEY:${item.quantity}"
        }
    }
}