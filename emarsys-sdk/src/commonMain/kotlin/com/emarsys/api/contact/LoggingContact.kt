package com.emarsys.api.contact

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class LoggingContact(private val logger: Logger) : ContactInstance {

    override suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::linkContact.name, buildJsonObject {
                put("contactFieldId", JsonPrimitive(contactFieldId))
                put("contactFieldValue", JsonPrimitive(contactFieldValue))
            }
        )
        logger.debug(entry)
    }

    override suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::linkAuthenticatedContact.name, buildJsonObject {
                put("contactFieldId", JsonPrimitive(contactFieldId))
                put("openIdToken", JsonPrimitive(openIdToken))
            }
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