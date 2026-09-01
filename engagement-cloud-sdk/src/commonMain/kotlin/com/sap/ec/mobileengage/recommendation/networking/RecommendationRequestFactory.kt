package com.sap.ec.mobileengage.recommendation.networking

import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.SdkEvent
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
            parameters.append("cp","1")

            when (webExtendEvent) {
                is SdkEvent.External.WebExtendEvent.Cart -> TODO()
                is SdkEvent.External.WebExtendEvent.CategoryView -> TODO()
                is SdkEvent.External.WebExtendEvent.ItemView -> parameters.append("v","i:${webExtendEvent.itemId}")
                is SdkEvent.External.WebExtendEvent.Purchase -> TODO()
                is SdkEvent.External.WebExtendEvent.RecommendationClick -> TODO()
                is SdkEvent.External.WebExtendEvent.Search -> TODO()
                is SdkEvent.External.WebExtendEvent.Tag -> TODO()
            }
        }

        return UrlRequest(
            url = url,
            method = HttpMethod.Get
        )
    }

}