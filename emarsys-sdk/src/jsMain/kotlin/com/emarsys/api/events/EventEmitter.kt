package com.emarsys.api.events

import EmarsysSdkEventListener
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.event.SdkEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.StringFormat
import kotlinx.serialization.encodeToString

internal class EventEmitter(
    private val sdkPublicEventFLow: Flow<SdkEvent.External.Api>,
    private val applicationScope: CoroutineScope,
    private val listeners: MutableMap<String, MutableList<EmarsysSdkEventListener>>,
    private val onceListeners: MutableMap<String, EmarsysSdkEventListener>,
    private val uuidProvider: UuidProviderApi,
    private val json: StringFormat,
    private val logger: Logger
) : EventEmitterApi {

    private var collectionJob: Job? = null

    override fun on(event: String, listener: EmarsysSdkEventListener) {
        registerListener(event, listener)
        sendListenerOperationLog("Registering listener for event type: $event")

        if (collectionJob == null) {
            collectionJob = applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
                sdkPublicEventFLow.collect { event ->
                    val parsedApiEvent = parseSdkPublicEvent(event)
                    val eventType = event.type

                    parsedApiEvent?.let { parsedEvent ->
                        listeners[eventType]?.forEach { listener ->
                            invokeListener(parsedEvent, listener)
                        }
                    }
                }
            }
        }
    }

    override fun once(event: String, listener: EmarsysSdkEventListener) {
        sendListenerOperationLog("Registering once listener for event type: $event")

        var hasBeenCalled = false
        val uuid = uuidProvider.provide()
        val onceListener: EmarsysSdkEventListener = { apiEvent ->
            if (!hasBeenCalled) {
                hasBeenCalled = true
                listener(apiEvent)
                onceListeners[uuid]?.let { off(event, it) }
                onceListeners.remove(uuid)
            }
        }

        onceListeners.put(uuid, onceListener)
        on(event, onceListener)
    }

    override fun off(event: String, listener: EmarsysSdkEventListener) {
        sendListenerOperationLog("Removing listener for event type: $event")

        listeners[event]?.remove(listener)
        if (listeners[event]?.isEmpty() == true) {
            listeners.remove(event)
        }
        stopCollectionOnEmptyMap()
    }

    override fun removeAllListeners() {
        sendListenerOperationLog("Registering all listeners.")
        listeners.clear()
        collectionJob?.cancel()
        collectionJob = null
    }

    private fun stopCollectionOnEmptyMap() {
        if (listeners.isEmpty() && collectionJob?.isActive == true) {
            collectionJob?.cancel()
            collectionJob = null
        }
    }

    private fun sendListenerOperationLog(message: String) {
        applicationScope.launch {
            logger.debug(message)
        }
    }

    private fun registerListener(event: String, listener: EmarsysSdkEventListener) {
        if (listeners[event] == null) {
            listeners[event] = mutableListOf(listener)
        } else {
            listeners[event]?.add(listener)
        }
    }

    private suspend fun parseSdkPublicEvent(event: SdkEvent.External.Api): SdkApiEvent? {
        return try {
            JSON.parse<SdkApiEvent>(json.encodeToString(event))
        } catch (error: Throwable) {
            logger.error("SdkEvent parsing failed", error)
            null
        }
    }

    private suspend fun invokeListener(event: SdkApiEvent, listener: EmarsysSdkEventListener) {
        try {
            listener(event)
        } catch (error: Throwable) {
            logger.error("Listener invocation failed for event type: ${event.type}", error, false)
        }
    }
}
