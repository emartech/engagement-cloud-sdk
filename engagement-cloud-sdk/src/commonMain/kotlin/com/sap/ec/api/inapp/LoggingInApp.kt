package com.sap.ec.api.inapp

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class LoggingInApp(private val sdkContext: SdkContextApi, private val logger: Logger) : InAppInstance {
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
        logger.debug("${this::class.simpleName} activated")
    }
}