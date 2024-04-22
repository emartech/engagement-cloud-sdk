package com.emarsys.api.predict

import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter

class PredictInternal: PredictInstance {
    override suspend fun trackCart(items: List<CartItem>) {
        TODO("Not yet implemented")
    }

    override suspend fun trackPurchase(orderId: String, items: List<CartItem>) {
        TODO("Not yet implemented")
    }

    override suspend fun trackItemView(itemId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun trackCategoryView(categoryPath: String) {
        TODO("Not yet implemented")
    }

    override suspend fun trackSearchTerm(searchTerm: String) {
        TODO("Not yet implemented")
    }

    override suspend fun trackTag(tag: String, attributes: Map<String, String>?) {
        TODO("Not yet implemented")
    }

    override suspend fun trackRecommendationClick(product: Product) {
        TODO("Not yet implemented")
    }

    override suspend fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>?,
        limit: Int?,
        availabilityZone: String?
    ): List<Product> {
        TODO("Not yet implemented")
    }

    override suspend fun activate() {
        TODO("Not yet implemented")
    }
}