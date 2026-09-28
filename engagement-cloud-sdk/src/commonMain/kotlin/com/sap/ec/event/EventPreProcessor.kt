package com.sap.ec.event

internal class EventPreProcessor<T : SdkEvent>(
    val preProcessors: List<EventPreProcessorApi<T>>
) : EventPreProcessorApi<T> {

    override suspend fun process(event: T) {
        preProcessors.forEach {
            it.process(event)
        }
    }
}