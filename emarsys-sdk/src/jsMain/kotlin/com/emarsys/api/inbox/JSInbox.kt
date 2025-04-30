package com.emarsys.api.inbox

import com.emarsys.api.inbox.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

class JSInbox(private val inboxApi: InboxApi, private val applicationScope: CoroutineScope) : JSInboxApi {
    override fun fetchMessages(): Promise<List<Message>> {
        return applicationScope.promise {
            inboxApi.fetchMessages().getOrThrow()
        }
    }

    override fun addTag(
        tag: String,
        messageId: String
    ): Promise<Unit> {
        return applicationScope.promise {
            inboxApi.addTag(tag, messageId).getOrThrow()
        }
    }

    override fun removeTag(
        tag: String,
        messageId: String
    ): Promise<Unit> {
        return applicationScope.promise {
            inboxApi.removeTag(tag, messageId).getOrThrow()
        }
    }
}