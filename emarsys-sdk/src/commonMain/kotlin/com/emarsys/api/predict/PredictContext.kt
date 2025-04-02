package com.emarsys.api.predict

import com.emarsys.api.predict.model.CartItem
import com.emarsys.api.predict.model.Logic
import com.emarsys.api.predict.model.Product
import com.emarsys.api.predict.model.RecommendationFilter
import kotlinx.serialization.Serializable

internal class PredictContext(override val calls: MutableList<PredictCall>) : PredictContextApi

@Serializable
sealed interface PredictCall {

    @Serializable
    data class TrackCart(val items: List<CartItem>) : PredictCall

    @Serializable
    data class TrackPurchase(val orderId: String, val items: List<CartItem>) : PredictCall

    @Serializable
    data class TrackItemView(val itemId: String) : PredictCall

    @Serializable
    data class TrackCategoryView(val categoryPath: String) : PredictCall

    @Serializable
    data class TrackSearchTerm(val searchTerm: String) : PredictCall

    @Serializable
    data class TrackTag(val tag: String, val attributes: Map<String, String>?) : PredictCall

    @Serializable
    data class TrackRecommendationClick(val product: Product) : PredictCall

    @Serializable
    data class RecommendProducts(
        val logic: Logic,
        val filters: List<RecommendationFilter>?,
        val limit: Int?,
        val availabilityZone: String?
    ) : PredictCall
}