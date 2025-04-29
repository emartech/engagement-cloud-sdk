package com.emarsys.api.predict

import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter
import com.emarsys.di.SdkKoinIsolationContext.koin

class IosPredict : IosPredictApi {
    override suspend fun trackCart(items: List<CartItem>) {
        koin.get<PredictApi>().trackCart(items).getOrThrow()
    }

    override suspend fun trackPurchase(
        orderId: String,
        items: List<CartItem>
    ) {
        koin.get<PredictApi>().trackPurchase(orderId, items).getOrThrow()
    }

    override suspend fun trackItemView(itemId: String) {
        koin.get<PredictApi>().trackItemView(itemId).getOrThrow()
    }

    override suspend fun trackCategoryView(categoryPath: String) {
        koin.get<PredictApi>().trackCategoryView(categoryPath).getOrThrow()
    }

    override suspend fun trackSearchTerm(searchTerm: String) {
        koin.get<PredictApi>().trackSearchTerm(searchTerm).getOrThrow()
    }

    override suspend fun trackTag(
        tag: String,
        attributes: Map<String, String>?
    ) {
        koin.get<PredictApi>().trackTag(tag, attributes).getOrThrow()
    }

    override suspend fun trackRecommendationClick(product: Product) {
        koin.get<PredictApi>().trackRecommendationClick(product).getOrThrow()
    }

    override suspend fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>?,
        limit: Int?,
        availabilityZone: String?
    ): List<Product> {
        return koin.get<PredictApi>().recommendProducts(logic, filters, limit, availabilityZone)
            .getOrThrow()
    }
}