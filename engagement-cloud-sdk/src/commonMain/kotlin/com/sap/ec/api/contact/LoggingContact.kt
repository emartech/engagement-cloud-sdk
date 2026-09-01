package com.sap.ec.api.contact

import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger

internal class LoggingContact(private val logger: Logger) : ContactInstance {

    override suspend fun link(contactFieldValue: String) {
        val entry = LogEntry.createMethodNotAllowed(this, this::link.name)
        logger.debug(entry)
    }

    override suspend fun linkAuthenticated(openIdToken: String) {
        val entry = LogEntry.createMethodNotAllowed(this, this::linkAuthenticated.name)
        logger.debug(entry)
    }

    override suspend fun unlink() {
        val entry = LogEntry.createMethodNotAllowed(this, this::unlink.name)
        logger.debug(entry)
    }

    override suspend fun activate() {
        logger.debug("${this::class.simpleName} activated")
    }

}