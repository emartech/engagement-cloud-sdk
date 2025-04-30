package com.emarsys.api.predict

import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

class JSPredict(
    private val predictApi: PredictApi,
    private val applicationScope: CoroutineScope
) : JSPredictApi {
    override fun trackCart(items: List<CartItem>): Promise<Unit> {
        return applicationScope.promise {
            predictApi.trackCart(items).getOrThrow()
        }
    }

    override fun trackPurchase(orderId: String, items: List<CartItem>): Promise<Unit> {
        return applicationScope.promise {
            predictApi.trackPurchase(orderId, items).getOrThrow()
        }
    }

    override fun trackItemView(itemId: String): Promise<Unit> {
        return applicationScope.promise {
            predictApi.trackItemView(itemId).getOrThrow()
        }
    }

    override fun trackCategoryView(categoryPath: String): Promise<Unit> {
        return applicationScope.promise {
            predictApi.trackCategoryView(categoryPath).getOrThrow()
        }
    }

    override fun trackSearchTerm(searchTerm: String): Promise<Unit> {
        return applicationScope.promise {
            predictApi.trackSearchTerm(searchTerm).getOrThrow()
        }
    }

    override fun trackTag(tag: String, attributes: Map<String, String>?): Promise<Unit> {
        return applicationScope.promise {
            predictApi.trackTag(tag, attributes).getOrThrow()
        }
    }

    override fun trackRecommendationClick(product: Product): Promise<Unit> {
        return applicationScope.promise {
            predictApi.trackRecommendationClick(product).getOrThrow()
        }
    }

    override fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>?,
        limit: Int?,
        availabilityZone: String?
    ): Promise<List<Product>> {
        return applicationScope.promise {
            predictApi.recommendProducts(logic, filters, limit, availabilityZone).getOrThrow()
        }
    }
}