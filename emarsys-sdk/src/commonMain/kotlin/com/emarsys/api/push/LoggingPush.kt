package com.emarsys.api.push

import com.emarsys.api.AppEvent
import com.emarsys.api.SdkResult
import com.emarsys.core.exceptions.MethodNotAllowedException
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class LoggingPush(
    private val logger: Logger,
    override val notificationEvents: MutableSharedFlow<AppEvent>
) : PushInstance {
    override suspend fun registerPushToken(pushToken: String): SdkResult {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::registerPushToken.name, mapOf(
                "pushToken" to pushToken,
            )
        )
        logger.log(entry, LogLevel.debug)
        return SdkResult.Failure(MethodNotAllowedException(entry))
    }

    override suspend fun clearPushToken(): SdkResult {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::clearPushToken.name
        )
        logger.log(entry, LogLevel.debug)
        return SdkResult.Failure(MethodNotAllowedException(entry))
    }

    override val pushToken: String?
        get() {
            val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
            logger.log(entry, LogLevel.debug)
            return null
        }


    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.log(entry, LogLevel.debug)
    }
}