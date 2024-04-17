package com.emarsys.api.inbox

import com.emarsys.api.inbox.model.Message

class InboxInternal: InboxInstance {
    override suspend fun fetchMessages(): List<Message> {
        TODO("Not yet implemented")
    }

    override suspend fun addTag(tag: String, messageId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeTag(tag: String, messageId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun activate() {
        TODO("Not yet implemented")
    }
}