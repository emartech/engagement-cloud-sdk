package com.sap.ec.recommendation.models

data class RecommendationResult(
    val recommendationsByFeature: Map<String, FeatureRecommendation>
)