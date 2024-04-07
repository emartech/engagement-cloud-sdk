package com.emarsys.core.log

interface Logger {

    fun log(entry: LogEntry, level: LogLevel)

    fun debug(entry: LogEntry)

    fun error(entry: LogEntry)
}