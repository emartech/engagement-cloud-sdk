package com.emarsys.mobileengage.session

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.lifecycle.LifecycleEvent
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.session.SessionContext
import com.emarsys.core.session.SessionId

import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal class MobileEngageSession(
    private val timestampProvider: InstantProvider,
    private val uuidProvider: UuidProviderApi,
    private val sessionContext: SessionContext,
    private val sdkContext: SdkContextApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkDispatcher: CoroutineDispatcher,
    private val sdkLogger: Logger
) : Session {

    override suspend fun subscribe(lifecycleWatchDog: LifecycleWatchDog) {
        CoroutineScope(sdkDispatcher).launch(
            start = CoroutineStart.UNDISPATCHED
        ) {
            lifecycleWatchDog.lifecycleEvents.collect { event ->
                when (event) {
                    LifecycleEvent.OnForeground -> startSession()
                    LifecycleEvent.OnBackground -> endSession()
                }
            }
        }
    }

    override suspend fun startSession() {
        if (canStartSession()) {
            val sessionStart = timestampProvider.provide()
            try {
                sdkEventDistributor.registerAndStoreEvent(
                    SdkEvent.Internal.Sdk.SessionStart(
                        id = uuidProvider.provide(),
                        timestamp = sessionStart
                    )
                )
            } catch (exception: Exception) {
                sdkLogger.error(
                    LogEntry(
                        "mobile-engage-session-start-request-failed",
                        buildJsonObject {
                            put(
                                "error",
                                JsonPrimitive(exception.message ?: "Start session failed.")
                            )
                        }
                    )
                )
                resetSessionContext()
            } finally {
                sessionContext.sessionStart = sessionStart.toEpochMilliseconds()
                sessionContext.sessionId = SessionId(uuidProvider.provide())
            }
        } else {
            sdkLogger.debug(LogEntry("mobile-engage-session-start-not-possible"))
        }
    }

    override suspend fun endSession() {
        if (canEndSession()) {
            return try {
                val event = createSessionEndEvent()
                sdkEventDistributor.registerAndStoreEvent(event)
            } catch (exception: Exception) {
                sdkLogger.error(
                    LogEntry(
                        "mobile-engage-session-end-request-failed",
                        buildJsonObject {
                            put(
                                "error",
                                JsonPrimitive(exception.message ?: "End session failed")
                            )
                        }
                    )
                )
            } finally {
                resetSessionContext()
            }
        } else {
            sdkLogger.debug(
                LogEntry("mobile-engage-session-end-not-possible")
            )
        }
    }

    private fun canStartSession() =
        sdkContext.config?.applicationCode != null
                && sessionContext.contactToken != null
                && sessionContext.sessionId == null
                && sessionContext.sessionStart == null

    private fun canEndSession() =
        sdkContext.config?.applicationCode != null
                && sessionContext.contactToken != null
                && sessionContext.sessionId != null
                && sessionContext.sessionStart != null

    private fun resetSessionContext() {
        sessionContext.sessionStart = null
        sessionContext.sessionId = null
    }

    private fun createSessionEndEvent(): SdkEvent {
        val sessionEnd = timestampProvider.provide()
        val duration =
            sessionEnd.toEpochMilliseconds() - sessionContext.sessionStart!!
        return SdkEvent.Internal.Sdk.SessionEnd(
            id = uuidProvider.provide(),
            attributes = buildJsonObject {
                put(
                    "duration",
                    JsonPrimitive(duration.toString())
                )
            },
            timestamp = sessionEnd
        )
    }
}