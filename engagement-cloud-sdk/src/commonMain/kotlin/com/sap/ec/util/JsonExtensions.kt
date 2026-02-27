package com.sap.ec.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

fun JsonObject.toMap(): Map<String, Any> {
    return runCatching {
        this.mapNotNull { (key, value) ->
            val convertedValue = value.toAny()
            if (convertedValue != null) {
                key to convertedValue
            } else {
                null
            }
        }.toMap()
    }.getOrElse { emptyMap() }
}

fun Map<String, Any?>.toJsonObject(): JsonObject {
    return runCatching {
        buildJsonObject {
            this@toJsonObject.forEach { (key, value) ->
                put(key, value.toJsonElement())
            }
        }
    }.getOrElse { buildJsonObject {} }
}

fun JsonElement.toAny(): Any? {
    return runCatching {
        when (this) {
            is JsonNull -> null
            is JsonPrimitive -> {
                when {
                    isString -> contentOrNull
                    booleanOrNull != null -> boolean
                    intOrNull != null -> int
                    longOrNull != null -> long
                    doubleOrNull != null -> double
                    else -> contentOrNull
                }
            }
            is JsonObject -> this.toMap()
            is JsonArray -> this.map { it.toAny() }
        }
    }.getOrNull()
}

fun Any?.toJsonElement(): JsonElement {
    return runCatching {
        when (this) {
            null -> JsonNull
            is JsonElement -> this
            is Map<*, *> -> {
                buildJsonObject {
                    @Suppress("UNCHECKED_CAST")
                    (this@toJsonElement as Map<String, Any?>).forEach { (key, value) ->
                        put(key, value.toJsonElement())
                    }
                }
            }
            is List<*> -> JsonArray(this.map { it.toJsonElement() })
            is String -> JsonPrimitive(this)
            is Number -> JsonPrimitive(this)
            is Boolean -> JsonPrimitive(this)
            is Char -> JsonPrimitive(this.toString())
            else -> JsonPrimitive(this.toString())
        }
    }.getOrElse { JsonPrimitive(this.toString()) }
}