package com.emarsys.core.log

interface Logger {

    fun log(entry: LogEntry, level: LogLevel)

}