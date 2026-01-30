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
    override val isUnreadFilterActive: Boolean
        get() {
            val entry = LogEntry.createMethodNotAllowed<LoggingEmbeddedMessaging>(
                this, this::categories.name
            )
            CoroutineScope(sdkContext.sdkDispatcher).launch {
                logger.debug(entry)
            }
            return false
        }
    override val activeCategoryIdFilters: Set<Int>
        get() {
            val entry = LogEntry.createMethodNotAllowed<LoggingEmbeddedMessaging>(
                this, this::categories.name
            )
            CoroutineScope(sdkContext.sdkDispatcher).launch {
                logger.debug(entry)
            }
            return emptySet()
        }

    override fun filterUnreadOnly(filterUnreadOnly: Boolean) {
        val entry = LogEntry.createMethodNotAllowed<LoggingEmbeddedMessaging>(
            this, this::categories.name
        )
        CoroutineScope(sdkContext.sdkDispatcher).launch {
            logger.debug(entry)
        }
    }

    override fun filterByCategoryIds(categoryIds: Set<Int>) {
        val entry = LogEntry.createMethodNotAllowed<LoggingEmbeddedMessaging>(
            this, this::categories.name
        )
        CoroutineScope(sdkContext.sdkDispatcher).launch {
            logger.debug(entry)
        }
    }

    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed<LoggingEmbeddedMessaging>(
            this, this::categories.name
        )
        CoroutineScope(sdkContext.sdkDispatcher).launch {
            logger.debug(entry)
        }
    }
}