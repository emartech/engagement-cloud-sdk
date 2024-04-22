package com.emarsys.api.predict

import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter

interface PredictApi {
    suspend fun trackCart(items: List<CartItem>): Result<Unit>
    suspend fun trackPurchase(orderId: String, items: List<CartItem>): Result<Unit>
    suspend fun trackItemView(itemId: String): Result<Unit>
    suspend fun trackCategoryView(categoryPath: String): Result<Unit>
    suspend fun trackSearchTerm(searchTerm: String): Result<Unit>
    suspend fun trackTag(tag: String, attributes: Map<String, String>?): Result<Unit>
    suspend fun trackRecommendationClick(product: Product): Result<Unit>

    suspend fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>? = null,
        limit: Int? = null,
        availabilityZone: String? = null
    ): Result<List<Product>>

}