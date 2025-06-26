package com.emarsys.networking.clients.error

import com.emarsys.networking.clients.event.model.OnlineSdkEvent

interface ClientExceptionHandler {

    suspend fun handleException(
        throwable: Throwable,
        errorMessage: String,
        vararg events: OnlineSdkEvent
    )
}
