package com.emarsys.api.predict

import Activatable
import com.emarsys.api.generic.GenericApi
import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter
import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.withContext

interface PredictInstance : PredictInternalApi, Activatable

internal class Predict<Logging : PredictInstance, Gatherer : PredictInstance, Internal : PredictInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(
    loggingApi, gathererApi, internalApi, sdkContext
), PredictApi {
    override suspend fun trackCart(items: List<CartItem>): Result<Unit> =
        activeInstance<PredictInstance>().runCatching {
            withContext(sdkContext.sdkDispatcher) {
                trackCart(items)
            }
        }

    override suspend fun trackPurchase(orderId: String, items: List<CartItem>): Result<Unit> =
        activeInstance<PredictInstance>().runCatching {
            withContext(sdkContext.sdkDispatcher) {
                trackPurchase(orderId, items)
            }
        }


    override suspend fun trackItemView(itemId: String): Result<Unit> =
        activeInstance<PredictInstance>().runCatching {
            withContext(sdkContext.sdkDispatcher) {
                trackItemView(itemId)
            }
        }

    override suspend fun trackCategoryView(categoryPath: String): Result<Unit> =
        activeInstance<PredictInstance>().runCatching {
            withContext(sdkContext.sdkDispatcher) {
                trackCategoryView(categoryPath)
            }
        }

    override suspend fun trackSearchTerm(searchTerm: String): Result<Unit> =
        activeInstance<PredictInstance>().runCatching {
            withContext(sdkContext.sdkDispatcher) {
                trackSearchTerm(searchTerm)
            }
        }

    override suspend fun trackTag(tag: String, attributes: Map<String, String>?): Result<Unit> =
        activeInstance<PredictInstance>().runCatching {
            withContext(sdkContext.sdkDispatcher) {
                trackTag(tag, attributes)
            }
        }

    override suspend fun trackRecommendationClick(product: Product): Result<Unit> =
        activeInstance<PredictInstance>().runCatching {
            withContext(sdkContext.sdkDispatcher) {
                trackRecommendationClick(product)
            }
        }

    override suspend fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>?,
        limit: Int?,
        availabilityZone: String?
    ): Result<List<Product>> = activeInstance<PredictInstance>().runCatching {
        withContext(sdkContext.sdkDispatcher) {
            recommendProducts(logic, filters, limit, availabilityZone)
        }
    }
}