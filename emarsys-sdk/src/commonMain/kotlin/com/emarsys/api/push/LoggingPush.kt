package com.emarsys.api.push

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger

class LoggingPush(private val logger: Logger) : PushInstance {
    override suspend fun registerPushToken(pushToken: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::registerPushToken.name, mapOf(
                "pushToken" to pushToken,
            )
        )
        logger.log(entry, LogLevel.debug)
    }

    override suspend fun clearPushToken() {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::clearPushToken.name
        )
        logger.log(entry, LogLevel.debug)
    }

    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.log(entry, LogLevel.debug)
    }
}