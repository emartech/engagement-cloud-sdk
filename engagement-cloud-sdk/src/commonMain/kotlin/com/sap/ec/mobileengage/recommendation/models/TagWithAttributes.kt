package com.sap.ec.mobileengage.recommendation.models

import kotlinx.serialization.Serializable

@Serializable
data class TagWithAttributes(
    val name: String,
    val attributes: Map<String, String>
)