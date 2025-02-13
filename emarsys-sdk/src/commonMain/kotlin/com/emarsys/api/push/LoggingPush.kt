package com.emarsys.api.push

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.core.storage.TypedStorageApi

open class LoggingPush(
    private val logger: Logger,
    private val storage: TypedStorageApi<String?>,
) : PushInstance {
    override suspend fun registerPushToken(pushToken: String) {
        storage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, pushToken)
        val entry = LogEntry.createMethodNotAllowed(
            this, this::registerPushToken.name, mapOf(
                "pushToken" to pushToken,
            )
        )
        logger.log(entry, LogLevel.Debug)
    }

    override suspend fun clearPushToken() {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::clearPushToken.name
        )
        logger.log(entry, LogLevel.Debug)
    }

    override val pushToken: String?
        get() {
            val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
            logger.log(entry, LogLevel.Debug)
            return null
        }


    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.log(entry, LogLevel.Debug)
    }
}