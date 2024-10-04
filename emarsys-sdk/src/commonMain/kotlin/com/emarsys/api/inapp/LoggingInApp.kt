package com.emarsys.api.inapp

import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LoggingInApp(private val sdkContext: SdkContextApi, private val logger: Logger) : InAppInstance {
    override val isPaused: Boolean
        get() {
            val entry = LogEntry.createMethodNotAllowed<LoggingInApp>(
                this, this::isPaused.name
            )
            CoroutineScope(sdkContext.sdkDispatcher).launch {
                logger.debug(entry)
            }
            return false
        }

    override suspend fun pause() {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::pause.name
        )
        logger.debug(entry)
    }

    override suspend fun resume() {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::resume.name
        )
        logger.debug(entry)
    }

    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::activate.name
        )
        logger.debug(entry)
    }
}