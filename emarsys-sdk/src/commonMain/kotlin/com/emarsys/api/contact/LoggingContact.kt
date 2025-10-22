package com.emarsys.api.contact

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal class LoggingContact(private val logger: Logger) : ContactInstance {

    override suspend fun link(contactFieldValue: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::link.name, buildJsonObject {
                put("contactFieldValue", JsonPrimitive(contactFieldValue))
            }
        )
        logger.debug(entry)
    }

    override suspend fun linkAuthenticated(openIdToken: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::linkAuthenticated.name, buildJsonObject {
                put("openIdToken", JsonPrimitive(openIdToken))
            }
        )
        logger.debug(entry)
    }

    override suspend fun unlink() {
        val entry = LogEntry.createMethodNotAllowed(this, this::unlink.name)
        logger.debug(entry)
    }

    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.debug(entry)
    }

}