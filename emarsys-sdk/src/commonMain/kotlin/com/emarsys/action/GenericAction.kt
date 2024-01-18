package com.emarsys.action

import kotlinx.serialization.Serializable

@Serializable
data class GenericAction(
    val type: String,
    val url: String?,
    val name: String?,
    val payload: Map<String, String>?,
    val method: String?,
    val value: Int?,
    val text: String?,
)