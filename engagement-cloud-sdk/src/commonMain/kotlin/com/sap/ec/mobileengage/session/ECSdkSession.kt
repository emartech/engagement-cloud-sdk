package com.sap.ec.mobileengage.session

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.lifecycle.LifecycleEvent
import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.core.session.SessionContext
import com.sap.ec.core.session.SessionId
import com.sap.ec.event.SdkEvent
import com.sap.ec.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ECSdkSession(
    private val timestampProvider: InstantProvider,
    private val uuidProvider: UuidProviderApi,
    private val requestContext: RequestContextApi,
    private val sessionContext: SessionContext,
    private val sdkContext: SdkContextApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkDispatcher: CoroutineDispatcher,
    private val sdkLogger: Logger
) : SessionApi {

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
                sdkEventDistributor.registerEvent(
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
            try {
                val event = createSessionEndEvent()
                sdkEventDistributor.registerEvent(event)
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
                && requestContext.contactToken != null
                && sessionContext.sessionId == null
                && sessionContext.sessionStart == null

    private fun canEndSession() =
        sdkContext.config?.applicationCode != null
                && requestContext.contactToken != null
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
            duration = duration,
            timestamp = sessionEnd
        )
    }
}