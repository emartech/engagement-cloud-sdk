package com.sap.ec.api.recommendation

import com.sap.ec.api.AutoRegisterable
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.product.Product

internal interface RecommendationApi: AutoRegisterable {
    suspend fun requestRecommendation(event: SdkEvent): Result<List<Product>>
}