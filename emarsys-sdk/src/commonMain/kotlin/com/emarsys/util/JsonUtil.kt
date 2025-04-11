package com.emarsys.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object JsonUtil {
    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        encodeDefaults = true
        isLenient = true
        ignoreUnknownKeys = true
        allowTrailingComma = true
        explicitNulls = false
        decodeEnumsCaseInsensitive = true
    }
}