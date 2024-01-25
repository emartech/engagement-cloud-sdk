package com.emarsys.api.predict

import com.emarsys.api.SdkResult
import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter

class Predict : PredictApi {
    override fun trackCart(items: List<CartItem>): SdkResult {
        TODO("Not yet implemented")
    }

    override fun trackPurchase(orderId: String, items: List<CartItem>): SdkResult {
        TODO("Not yet implemented")
    }

    override fun trackItemView(itemId: String): SdkResult {
        TODO("Not yet implemented")
    }

    override fun trackCategoryView(categoryPath: String): SdkResult {
        TODO("Not yet implemented")
    }

    override fun trackSearchTerm(searchTerm: String): SdkResult {
        TODO("Not yet implemented")
    }

    override fun trackTag(tag: String, attributes: Map<String, String>?): SdkResult {
        TODO("Not yet implemented")
    }

    override fun recommendProducts(logic: Logic): SdkResult {
        TODO("Not yet implemented")
    }

    override fun recommendProducts(logic: Logic, availabilityZone: String): SdkResult {
        TODO("Not yet implemented")
    }

    override fun recommendProducts(logic: Logic, limit: Int): SdkResult {
        TODO("Not yet implemented")
    }

    override fun recommendProducts(logic: Logic, limit: Int, availabilityZone: String): SdkResult {
        TODO("Not yet implemented")
    }

    override fun recommendProducts(logic: Logic, filters: List<RecommendationFilter>): SdkResult {
        TODO("Not yet implemented")
    }

    override fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>,
        availabilityZone: String
    ): SdkResult {
        TODO("Not yet implemented")
    }

    override fun recommendProducts(logic: Logic, filters: List<RecommendationFilter>, limit: Int): SdkResult {
        TODO("Not yet implemented")
    }

    override fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>,
        limit: Int,
        availabilityZone: String
    ): SdkResult {
        TODO("Not yet implemented")
    }

    override fun trackRecommendationClick(product: Product): SdkResult {
        TODO("Not yet implemented")
    }
}