package com.emarsys.api.embeddedmessaging

import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class LoggingEmbeddedMessaging(
    private val sdkContext: SdkContextApi,
    private val logger: Logger
) : EmbeddedMessagingInstance {
    override val categories: List<MessageCategory>
        get() {
            val entry = LogEntry.createMethodNotAllowed<LoggingEmbeddedMessaging>(
                this, this::categories.name
            )
            CoroutineScope(sdkContext.sdkDispatcher).launch {
                logger.debug(entry)
            }
            return listOf()
        }

    override suspend fun activate() {}
}