package com.emarsys.api.contact

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger

class LoggingContact(private val logger: Logger) : ContactInstance {

    override suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::linkContact.name, mapOf(
                "contactFieldId" to contactFieldId,
                "contactFieldValue" to contactFieldValue
            )
        )
        logger.debug(entry)
    }

    override suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::linkAuthenticatedContact.name, mapOf(
                "contactFieldId" to contactFieldId,
                "openIdToken" to openIdToken
            )
        )
        logger.debug(entry)
    }

    override suspend fun unlinkContact() {
        val entry = LogEntry.createMethodNotAllowed(this, this::unlinkContact.name)
        logger.debug(entry)
    }

    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.debug(entry)
    }

}