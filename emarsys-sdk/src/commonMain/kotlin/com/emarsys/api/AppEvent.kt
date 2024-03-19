package com.emarsys.api

data class AppEvent(
    val name: String,
    val payload: Map<String, String>?
)