package com.emarsys.core.log

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class LogContext(val contextMap: Map<String, Any>) :
    AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<LogContext>
}

suspend fun <T> withLogContext(
    contextMap: Map<String, Any>,
    block: suspend CoroutineScope.() -> T
): T {
    val extendedContext = mutableMapOf<String, Any>()
    extendedContext.putAll(contextMap)
    coroutineContext[LogContext.Key]?.contextMap?.let {
        extendedContext.putAll(it)
    }
    return withContext(coroutineContext + extendedContext.toContext()) {
        block()
    }
}

fun Map<String, Any>.toContext(): CoroutineContext {
    return LogContext(this)
}