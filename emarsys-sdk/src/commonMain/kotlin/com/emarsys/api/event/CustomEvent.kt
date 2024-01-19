package com.emarsys.api.event

data class CustomEvent(
    val name: String,
    val attributes: Map<String, String>? = null
)
