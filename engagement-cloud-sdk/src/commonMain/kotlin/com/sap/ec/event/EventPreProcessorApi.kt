package com.sap.ec.event

internal interface EventPreProcessorApi<T: SdkEvent> {
    suspend fun process(event: T)
}