package com.emarsys.core.log

class ConsoleLogger {

    fun logToConsole(level: LogLevel, log: String) {
        val color = getLogColor(level)
        println(colorizeLog(log, color))
    }

    private fun colorizeLog(log: String, color: String): String {
        return log.split("\n").joinToString("\n") { "$color$it${"\u001B[0m"}" }
    }

    private fun getLogColor(level: LogLevel): String {
        return when (level) {
            LogLevel.Info -> "\u001B[0m"
            LogLevel.Trace -> "\u001B[35m"
            LogLevel.Debug -> "\u001B[34m"
            LogLevel.Error -> "\u001b[31m"
            LogLevel.Metric -> "\u001B[30m"
        }
    }
}