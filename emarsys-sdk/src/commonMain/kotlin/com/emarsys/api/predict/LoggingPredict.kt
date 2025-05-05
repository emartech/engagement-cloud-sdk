package com.emarsys.api.predict

import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal class LoggingPredict(private val logger: Logger) : PredictInstance {
    override suspend fun trackCart(items: List<CartItem>) {
        val entry = LogEntry.createMethodNotAllowed(
            this,
            this::trackCart.name,
            buildJsonObject { put("items", JsonPrimitive(items.joinToString(","))) }
        )
        logger.debug(entry)
    }

    override suspend fun trackPurchase(orderId: String, items: List<CartItem>) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::trackPurchase.name, buildJsonObject {
                put("orderId", JsonPrimitive(orderId))
                put("items", JsonPrimitive(items.joinToString(",")))
            })
        logger.debug(entry)
    }

    override suspend fun trackItemView(itemId: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::trackItemView.name, buildJsonObject {
                put("itemId", JsonPrimitive(itemId))
            }
        )
        logger.debug(entry)
    }

    override suspend fun trackCategoryView(categoryPath: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::trackCategoryView.name, buildJsonObject {
                put("categoryPath", JsonPrimitive(categoryPath))
            }
        )
        logger.debug(entry)
    }

    override suspend fun trackSearchTerm(searchTerm: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this,
            this::trackSearchTerm.name,
            buildJsonObject {
                put("searchTerm", JsonPrimitive(searchTerm))
            }
        )
        logger.debug(entry)
    }

    override suspend
    fun trackTag(tag: String, attributes: Map<String, String>?) {
        val params = buildJsonObject {
            put("tag", JsonPrimitive(tag))
            put("attributes", JsonPrimitive(attributes.toString()))
        }

        val entry = LogEntry.createMethodNotAllowed(
            this, this::trackTag.name, params
        )
        logger.debug(entry)
    }

    override suspend
    fun trackRecommendationClick(product: Product) {
        val entry = LogEntry.createMethodNotAllowed(
            this,
            this::trackRecommendationClick.name,
            buildJsonObject { put("product", JsonPrimitive(product.toString())) }
        )
        logger.debug(entry)
    }

    override suspend
    fun recommendProducts(
        logic: Logic,
        filters: List<RecommendationFilter>?,
        limit: Int?,
        availabilityZone: String?
    ): List<Product> {
        val params = mapRecommendationParams(
            logic,
            filters,
            limit,
            availabilityZone
        )

        val entry = LogEntry.createMethodNotAllowed(
            this,
            this::recommendProducts.name,
            buildJsonObject { put("params", JsonPrimitive(params.toString())) }
        )
        logger.debug(entry)
        return listOf()
    }

    override suspend
    fun activate() {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::activate.name
        )
        logger.debug(entry)
    }

    private fun mapRecommendationParams(
        logic: Logic,
        filters: List<RecommendationFilter>?,
        limit: Int?,
        availabilityZone: String?
    ): Map<String, Any> {
        val data = mutableMapOf<String, Any>("logic" to logic)

        filters?.let {
            data.put("filters", it)
        }
        limit?.let { data.put("limit", it) }
        availabilityZone?.let { data.put("availabilityZone", it) }

        return data
    }
}