package com.sap.ec.networking.clients.recommendation

import com.sap.ec.recommendation.models.product.Feature
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class RecommendationResponse(
    val cohort: String?,
    val features: Map<String, Feature> = emptyMap(),
    val products: Map<String, JsonObject> = emptyMap()
)