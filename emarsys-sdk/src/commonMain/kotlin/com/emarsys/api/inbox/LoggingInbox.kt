package com.emarsys.api.inbox

import com.emarsys.api.inbox.model.Message
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class LoggingInbox(private val logger: Logger) : InboxInstance {
    override suspend fun fetchMessages(): List<Message> {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::fetchMessages.name
        )
        logger.debug(entry)
        return emptyList()
    }

    override suspend fun addTag(tag: String, messageId: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::addTag.name, buildJsonObject {
                put("tag", JsonPrimitive(tag))
                put("messageId", JsonPrimitive(messageId))
            })
        logger.debug(entry)
    }

    override suspend fun removeTag(tag: String, messageId: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::removeTag.name, buildJsonObject {
                put("tag", JsonPrimitive(tag))
                put("messageId", JsonPrimitive(messageId))
            }
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