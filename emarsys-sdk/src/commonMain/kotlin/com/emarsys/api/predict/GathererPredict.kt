package com.emarsys.api.predict

import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter

class GathererPredict(private val predictContext: PredictContext): PredictInstance {
    override suspend fun trackCart(items: List<CartItem>) {
        predictContext.calls.add(PredictCall.TrackCart(items))
    }

    override suspend fun trackPurchase(orderId: String, items: List<CartItem>) {
        predictContext.calls.add(PredictCall.TrackPurchase(orderId, items))
    }

    override suspend fun trackItemView(itemId: String) {
        predictContext.calls.add(PredictCall.TrackItemView(itemId))
    }

    override suspend fun trackCategoryView(categoryPath: String) {
        predictContext.calls.add(PredictCall.TrackCategoryView(categoryPath))
    }

    override suspend fun trackSearchTerm(searchTerm: String) {
        predictContext.calls.add(PredictCall.TrackSearchTerm(searchTerm))
    }

    override suspend fun trackTag(tag: String, attributes: Map<String, String>?) {
        predictContext.calls.add(PredictCall.TrackTag(tag, attributes))
    }

    override suspend fun trackRecommendationClick(product: Product) {
        predictContext.calls.add(PredictCall.TrackRecommendationClick(product))
    }

    override suspend fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>?,
        limit: Int?,
        availabilityZone: String?
    ): List<Product> {
        predictContext.calls.add(PredictCall.RecommendProducts(logic, filters, limit, availabilityZone))
        return listOf()
    }

    override suspend fun activate() {}
}