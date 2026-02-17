package com.sap.ec.networking.clients.error

import com.sap.ec.event.OnlineSdkEvent

interface ClientExceptionHandler {

    suspend fun handleException(
        throwable: Throwable,
        errorMessage: String,
        vararg events: OnlineSdkEvent
    )
}
