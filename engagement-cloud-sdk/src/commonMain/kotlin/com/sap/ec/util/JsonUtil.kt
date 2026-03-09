package com.sap.ec.util

import com.sap.ec.InternalSdkApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@InternalSdkApi
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