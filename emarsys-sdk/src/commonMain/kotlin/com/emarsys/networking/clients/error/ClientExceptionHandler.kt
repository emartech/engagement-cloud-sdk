package com.emarsys.networking.clients.error

import com.emarsys.event.OnlineSdkEvent

interface ClientExceptionHandler {

    suspend fun handleException(
        throwable: Throwable,
        errorMessage: String,
        vararg events: OnlineSdkEvent
    )
}
