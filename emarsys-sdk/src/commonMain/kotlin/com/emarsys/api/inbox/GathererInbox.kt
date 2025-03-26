package com.emarsys.api.inbox

import com.emarsys.api.inbox.model.Message

internal class GathererInbox(private val inboxContext: InboxContextApi) : InboxInstance {
    override suspend fun fetchMessages(): List<Message> {
        inboxContext.calls.add(InboxCall.FetchMessages())
        return inboxContext.messages
    }

    override suspend fun addTag(tag: String, messageId: String) {
        inboxContext.calls.add(InboxCall.AddTag(tag, messageId))
    }

    override suspend fun removeTag(tag: String, messageId: String) {
        inboxContext.calls.add(InboxCall.RemoveTag(tag, messageId))
    }

    override suspend fun activate() {}
}