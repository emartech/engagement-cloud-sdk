package com.sap.ec.api.recommendation

import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.product.Product

internal interface RecommendationInternalApi {
    suspend fun requestRecommendation(event: SdkEvent): List<Product>
}