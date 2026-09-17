package com.sap.ec.recommendation

data class RecommendationOptions(
    val logic: RecommendationLogic,
    val limit: Int = 5,
    val offset: Int = 0,
    val filters: List<RecommendationFilter>? = null,
    val availabilityZone: String? = null,
    val language: String? = null,
    val displayCurrency: String? = null
)