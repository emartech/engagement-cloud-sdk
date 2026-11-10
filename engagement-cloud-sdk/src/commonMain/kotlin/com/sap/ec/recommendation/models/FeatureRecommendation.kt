package com.sap.ec.recommendation.models

import com.sap.ec.recommendation.models.product.Product

data class FeatureRecommendation (
    val featureName: String,
    val topicLabel: String?,
    val hasMore: Boolean,
    val merchants: List<String>,
    val products: List<Product>
)