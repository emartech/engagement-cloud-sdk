package com.sap.ec.api.config

import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal class LoggingConfig(private val logger: Logger) : ConfigInstance {
    override suspend fun changeApplicationCode(applicationCode: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::changeApplicationCode.name, buildJsonObject {
                put("applicationCode", JsonPrimitive(applicationCode))
            }
        )
        logger.debug(entry)
    }

    override suspend fun setLanguage(language: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::setLanguage.name, buildJsonObject {
                put("language", JsonPrimitive(language))
            }
        )
        logger.debug(entry)
    }

    override suspend fun resetLanguage() {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::resetLanguage.name, buildJsonObject {}
        )
        logger.debug(entry)
    }

    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.debug(entry)
    }
}