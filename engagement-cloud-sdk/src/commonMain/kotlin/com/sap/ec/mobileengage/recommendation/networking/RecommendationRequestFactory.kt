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
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.ITEM_ID_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.ITEM_VIEW_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.ORDER_ID_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.SEARCH_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.TAG_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.TAG_WITH_ATTRIBUTES_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.VIEW_CATEGORY_KEY
import com.sap.ec.mobileengage.recommendation.models.TagWithAttributes
import com.sap.ec.util.JsonUtil
import com.sap.ec.webExtend.CartItem
import io.ktor.http.HttpMethod
import io.ktor.http.buildUrl
import io.ktor.http.takeFrom

internal class RecommendationRequestFactory(
    private val urlFactory: UrlFactoryApi
) : RecommendationRequestFactoryApi {

    override suspend fun create(webExtendEvent: SdkEvent.External.WebExtendEvent): UrlRequest {
        val baseUrlWithAppCode = urlFactory.create(ECUrlType.Recommendation)
        val url = buildUrl {
            takeFrom(baseUrlWithAppCode)
            when (webExtendEvent) {
                is SdkEvent.External.WebExtendEvent.Cart -> {
                    parameters.append(CART_VERSION_FLAG_KEY, "1")
                    parameters.append(
                        CART_ITEMS_KEY,
                        webExtendEvent.items.toUrlParamValue()
                    )
                }

                is SdkEvent.External.WebExtendEvent.CategoryView -> parameters.append(
                    VIEW_CATEGORY_KEY,
                    webExtendEvent.categoryPath
                )

                is SdkEvent.External.WebExtendEvent.ItemView -> parameters.append(
                    ITEM_VIEW_KEY,
                    "$ITEM_ID_KEY:${webExtendEvent.itemId}"
                )

                is SdkEvent.External.WebExtendEvent.Purchase -> {
                    parameters.append(ORDER_ID_KEY, webExtendEvent.orderId)
                    parameters.append(
                        CHECKOUT_ITEMS_KEY,
                        webExtendEvent.items.toUrlParamValue()
                    )
                }

                is SdkEvent.External.WebExtendEvent.RecommendationClick -> TODO()
                is SdkEvent.External.WebExtendEvent.Search -> parameters.append(
                    SEARCH_KEY,
                    webExtendEvent.searchTerm
                )

                is SdkEvent.External.WebExtendEvent.Tag -> {
                    webExtendEvent.attributes?.let {
                        parameters.append(
                            TAG_WITH_ATTRIBUTES_KEY,
                            JsonUtil.json.encodeToString(
                                TagWithAttributes(
                                    webExtendEvent.tag,
                                    webExtendEvent.attributes
                                )
                            )
                        )
                    } ?: parameters.append(TAG_KEY, webExtendEvent.tag)
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
            "$ITEM_ID_KEY:${item.itemId},$CART_LIST_ITEM_PRICE_KEY:${item.price},$CART_LIST_ITEM_QUANTITY_KEY:${item.quantity}"
        }
    }
}