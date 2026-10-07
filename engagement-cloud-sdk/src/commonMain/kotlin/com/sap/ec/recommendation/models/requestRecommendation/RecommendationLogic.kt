package com.sap.ec.recommendation.models.requestRecommendation

sealed interface RecommendationLogic {
    data object Related : RecommendationLogic
    data object AlsoBought : RecommendationLogic
    data object Cart : RecommendationLogic
    data object Category : RecommendationLogic
    data object Popular : RecommendationLogic
    data object Search : RecommendationLogic
    data object Personal : RecommendationLogic
    data object Home : RecommendationLogic
    data class Custom(val name: String) : RecommendationLogic

    fun getLogicName(): String {
        return if (this is Custom) {
            this.name
        } else {
            this::class.simpleName?.uppercase() ?: "UNKNOWN"
        }
    }
}