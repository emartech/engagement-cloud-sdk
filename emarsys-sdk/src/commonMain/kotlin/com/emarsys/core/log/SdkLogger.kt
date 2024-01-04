package com.emarsys.core.log

class SdkLogger: Logger {

    override fun log(entry: LogEntry, level: LogLevel) {
        println("LogEntry: $entry logLevel: $level")
    }

}