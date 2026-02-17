package com.sap.ec.core.log

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.reflect.typeOf

data class LogEntry(
    val topic: String,
    val data: JsonObject = JsonObject(mapOf())
) {

    companion object {
        inline fun <reified Source> createMethodNotAllowed(
            source: Source,
            functionName: String,
            params: JsonObject? = null
        ): LogEntry {
            val data = buildJsonObject {
                put("className", JsonPrimitive(typeOf<Source>().toString()))
                put("functionName", JsonPrimitive(functionName))

                params?.let {
                    put("parameters", it)
                }
            }
            return LogEntry("log_method_not_allowed", JsonObject(data))
        }
    }

}
