package com.sap.ec.core.log

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class LogContext(val contextMap: JsonObject) :
    AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<LogContext>
}

suspend fun <T> withLogContext(
    contextMap: JsonObject,
    block: suspend CoroutineScope.() -> T
): T {
    val extendedContext = buildJsonObject {
        contextMap.forEach { contextMapEntry -> put(contextMapEntry.key, contextMapEntry.value) }
        currentCoroutineContext()[LogContext.Key]?.contextMap?.let { coroutineContextEntries ->
            coroutineContextEntries.entries.forEach {
                put(it.key, it.value)
            }
        }
    }
    return withContext(currentCoroutineContext() + extendedContext.toContext()) {
        block()
    }
}

fun JsonObject.toContext(): CoroutineContext {
    return LogContext(this)
}