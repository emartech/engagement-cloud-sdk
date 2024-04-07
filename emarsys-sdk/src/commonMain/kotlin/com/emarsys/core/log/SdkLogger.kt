package com.emarsys.core.log

class SdkLogger: Logger {

    override fun log(entry: LogEntry, level: LogLevel) {
        println("LogEntry: $entry logLevel: $level")
    }

    override fun debug(entry: LogEntry) {
        log(entry, LogLevel.Debug)
    }

    override fun error(entry: LogEntry) {
        log(entry, LogLevel.Error)
    }

}