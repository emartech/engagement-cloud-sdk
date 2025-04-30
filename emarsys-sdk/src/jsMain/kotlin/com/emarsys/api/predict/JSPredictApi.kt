package com.emarsys.api.predict

import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSPredictApi {
    fun trackCart(items: List<CartItem>): Promise<Unit>
    fun trackPurchase(orderId: String, items: List<CartItem>): Promise<Unit>
    fun trackItemView(itemId: String): Promise<Unit>
    fun trackCategoryView(categoryPath: String): Promise<Unit>
    fun trackSearchTerm(searchTerm: String): Promise<Unit>
    fun trackTag(tag: String, attributes: Map<String, String>?): Promise<Unit>
    fun trackRecommendationClick(product: Product): Promise<Unit>

    fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>? = null,
        limit: Int? = null,
        availabilityZone: String? = null
    ): Promise<List<Product>>
}