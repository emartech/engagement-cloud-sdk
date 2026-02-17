package com.sap.ec.networking.clients.error

import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.exceptions.SdkException.FailedRequestException
import com.sap.ec.core.exceptions.SdkException.MissingApplicationCodeException
import com.sap.ec.core.exceptions.SdkException.RetryLimitReachedException
import com.sap.ec.core.log.Logger
import com.sap.ec.event.OnlineSdkEvent

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
