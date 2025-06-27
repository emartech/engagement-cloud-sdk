package com.emarsys.networking.clients.error

import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.MissingApplicationCodeException
import com.emarsys.core.exceptions.RetryLimitReachedException
import com.emarsys.core.log.Logger
import com.emarsys.event.OnlineSdkEvent

internal class DefaultClientExceptionHandler(
    private val eventsDao: EventsDaoApi,
    private val sdkLogger: Logger
) : ClientExceptionHandler {

    override suspend fun handleException(
        throwable: Throwable,
        errorMessage: String,
        vararg events: OnlineSdkEvent
    ) {
        when (throwable) {
            is FailedRequestException, is RetryLimitReachedException, is MissingApplicationCodeException -> events.forEach {
                it.ack(eventsDao, sdkLogger)
            }

            else -> {
                sdkLogger.error(errorMessage, throwable)
                events.forEach {
                    it.nack(eventsDao, sdkLogger)
                }
            }
        }
    }
}
