package com.emarsys.api.predict

import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter

interface PredictInternalApi {
    suspend fun trackCart(items: List<CartItem>)
    suspend fun trackPurchase(orderId: String, items: List<CartItem>)
    suspend fun trackItemView(itemId: String)
    suspend fun trackCategoryView(categoryPath: String)
    suspend fun trackSearchTerm(searchTerm: String)
    suspend fun trackTag(tag: String, attributes: Map<String, String>?)
    suspend fun trackRecommendationClick(product: Product)

    suspend fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>? = null,
        limit: Int? = null,
        availabilityZone: String? = null
    ): List<Product>
}