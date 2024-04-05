package com.emarsys.core.log

interface Logger {

    fun log(entry: LogEntry, level: LogLevel)

    fun debug(entry: LogEntry) {
        log(entry, LogLevel.Debug)
    }

    fun error(entry: LogEntry) {
        log(entry, LogLevel.Error)
    }

}