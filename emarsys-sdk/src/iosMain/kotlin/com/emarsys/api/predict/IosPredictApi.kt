package com.emarsys.api.predict

import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter
import kotlin.coroutines.cancellation.CancellationException

interface IosPredictApi {
    @Throws(CancellationException::class)
    suspend fun trackCart(items: List<CartItem>)

    @Throws(CancellationException::class)
    suspend fun trackPurchase(orderId: String, items: List<CartItem>)

    @Throws(CancellationException::class)
    suspend fun trackItemView(itemId: String)

    @Throws(CancellationException::class)
    suspend fun trackCategoryView(categoryPath: String)

    @Throws(CancellationException::class)
    suspend fun trackSearchTerm(searchTerm: String)

    @Throws(CancellationException::class)
    suspend fun trackTag(tag: String, attributes: Map<String, String>?)

    @Throws(CancellationException::class)
    suspend fun trackRecommendationClick(product: Product)

    @Throws(CancellationException::class)
    suspend fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>? = null,
        limit: Int? = null,
        availabilityZone: String? = null
    ): List<Product>
}