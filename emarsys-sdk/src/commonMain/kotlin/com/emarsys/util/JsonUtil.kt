package com.emarsys.util

import kotlinx.serialization.json.Json

object JsonUtil {
    val json = Json {
        encodeDefaults = true
        isLenient = true
        ignoreUnknownKeys = true
    }
}