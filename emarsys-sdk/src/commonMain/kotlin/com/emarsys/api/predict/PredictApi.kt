package com.emarsys.api.predict

import com.emarsys.api.SdkResult
import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter

interface PredictApi {
    fun trackCart(items: List<CartItem>): SdkResult
    fun trackPurchase(orderId: String, items: List<CartItem>): SdkResult

    fun trackItemView(itemId: String): SdkResult
    fun trackCategoryView(categoryPath: String): SdkResult
    fun trackSearchTerm(searchTerm: String): SdkResult
    fun trackTag(tag: String, attributes: Map<String, String>?): SdkResult
    fun recommendProducts(logic: Logic): SdkResult
    fun recommendProducts(logic: Logic, availabilityZone: String): SdkResult
    fun recommendProducts(logic: Logic, limit: Int): SdkResult
    fun recommendProducts(
        logic: Logic,
        limit: Int,
        availabilityZone: String
    ): SdkResult

    fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>
    ): SdkResult

    fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>,
        availabilityZone: String
    ): SdkResult

    fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>,
        limit: Int
    ): SdkResult

    fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>,
        limit: Int,
        availabilityZone: String
    ): SdkResult

    fun trackRecommendationClick(product: Product): SdkResult
}