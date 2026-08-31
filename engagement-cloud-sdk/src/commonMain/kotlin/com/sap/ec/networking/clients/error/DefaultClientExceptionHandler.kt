package com.sap.ec.networking.clients.error

import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.exceptions.SdkException.FailedRequestException
import com.sap.ec.core.exceptions.SdkException.InvalidApplicationCodeException
import com.sap.ec.core.exceptions.SdkException.MissingApplicationCodeException
import com.sap.ec.core.exceptions.SdkException.RetryLimitReachedException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.body
import com.sap.ec.event.OnlineSdkEvent

internal class DefaultClientExceptionHandler(
    private val eventsDao: EventsDaoApi,
    private val sdkLogger: Logger
) : ClientExceptionHandler {

    private companion object {
        const val RESPONSE_ERROR_CODE_INVALID_APPLICATION = "1002"
    }

    override fun transformException(throwable: Throwable): Throwable {
        when (throwable) {
            is FailedRequestException -> {
                runCatching { throwable.response.body<ResponseErrorBody>() }.getOrNull()?.let {
                    if (it.error.code == RESPONSE_ERROR_CODE_INVALID_APPLICATION) {
                        return InvalidApplicationCodeException("Invalid application code")
                    }
                }
            }
        }
        return throwable
    }

    override suspend fun handleException(
        throwable: Throwable,
        errorMessage: String,
        vararg events: OnlineSdkEvent
    ) {
        when (throwable) {
            is FailedRequestException, is RetryLimitReachedException, is MissingApplicationCodeException -> events.forEach {
                sdkLogger.error(errorMessage, throwable, isRemoteLog = false)
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
