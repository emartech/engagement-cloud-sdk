package com.emarsys.core.log

import kotlinx.coroutines.currentCoroutineContext
import kotlin.native.concurrent.ThreadLocal
import kotlin.reflect.typeOf

data class LogEntry(
    val topic: String,
    val data: Map<String, Any>
) {
    
    companion object {
        inline fun <reified Source>createMethodNotAllowed(source: Source, functionName: String, params: Map<String, Any>? = null): LogEntry {
            val data = mutableMapOf<String, Any>(
                "className" to typeOf<Source>().toString(),
                "functionName" to functionName
            )
            params?.let {
                data.put("parameters", it)
            }
            return LogEntry("log_method_not_allowed", data)
        }
    }
    
}
