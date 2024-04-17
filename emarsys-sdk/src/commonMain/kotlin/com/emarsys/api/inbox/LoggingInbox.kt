package com.emarsys.api.inbox

import com.emarsys.api.inbox.model.Message
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger

class LoggingInbox(private val logger: Logger): InboxInstance {
    override suspend fun fetchMessages(): List<Message> {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::fetchMessages.name
        )
        logger.debug(entry)
        return emptyList()
    }

    override suspend fun addTag(tag: String, messageId: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::addTag.name, mapOf(
                "tag" to tag,
                "messageId" to messageId
            )
        )
        logger.debug(entry)
    }

    override suspend fun removeTag(tag: String, messageId: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::removeTag.name, mapOf(
                "tag" to tag,
                "messageId" to messageId
            )
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