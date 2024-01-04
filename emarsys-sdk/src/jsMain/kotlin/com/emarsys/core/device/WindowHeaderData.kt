package com.emarsys.core.device

import kotlinx.serialization.Serializable

@Serializable
data class WindowHeaderData(
    val osName: String,
    val osVersion: String,
    val browserName: String,
    val browserVersion: String
)