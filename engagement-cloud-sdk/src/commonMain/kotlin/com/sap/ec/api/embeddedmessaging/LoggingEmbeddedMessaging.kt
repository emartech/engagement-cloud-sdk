package com.sap.ec.api.embeddedmessaging

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory
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
    override val activeCategoryFilters: Set<MessageCategory>
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

    override fun filterByCategories(categories: Set<MessageCategory>) {
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