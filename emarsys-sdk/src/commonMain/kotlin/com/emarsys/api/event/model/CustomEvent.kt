package com.emarsys.api.event.model

data class CustomEvent(
    val name: String,
    val attributes: Map<String, String>? = null
)
