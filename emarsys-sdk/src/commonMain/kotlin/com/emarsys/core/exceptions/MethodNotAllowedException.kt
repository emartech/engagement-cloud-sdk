package com.emarsys.core.exceptions

import com.emarsys.core.log.LogEntry

class MethodNotAllowedException(private val entry: LogEntry) : Exception() {
    override val message: String
        get() = "Method not allowed: ${entry.data}"
}