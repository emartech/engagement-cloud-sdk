package com.emarsys.api.inbox

import Activatable
import com.emarsys.api.generic.GenericApi
import com.emarsys.api.inbox.model.Message
import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.withContext

internal interface InboxInstance : InboxInternalApi, Activatable

internal class Inbox<Logging : InboxInstance, Gatherer : InboxInstance, Internal : InboxInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(
    loggingApi, gathererApi, internalApi, sdkContext
), InboxApi {
    override suspend fun fetchMessages(): Result<List<Message>> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<InboxInstance>().fetchMessages()
        }
    }

    override suspend fun addTag(tag: String, messageId: String): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<InboxInstance>().addTag(tag, messageId)
        }
    }

    override suspend fun removeTag(tag: String, messageId: String): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<InboxInstance>().removeTag(tag, messageId)
        }
    }
}