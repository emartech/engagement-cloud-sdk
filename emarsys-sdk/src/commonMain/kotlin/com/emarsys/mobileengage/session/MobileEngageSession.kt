package com.emarsys.mobileengage.session

import com.emarsys.context.SdkContextApi
import com.emarsys.core.lifecycle.LifecycleEvent
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.Provider
import com.emarsys.core.session.SessionContext
import com.emarsys.core.session.SessionId
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class MobileEngageSession(
    private val timestampProvider: Provider<Instant>,
    private val uuidProvider: Provider<String>,
    private val sessionContext: SessionContext,
    private val sdkContext: SdkContextApi,
    private val eventClient: EventClientApi,
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
                eventClient.registerEvent(Event.createSessionStart(sessionStart))
            } catch (exception: Exception) {
                sdkLogger.error(
                    LogEntry(
                        "mobile-engage-session-start-request-failed",
                        mapOf("error" to (exception.message ?: "Start session failed."))
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
                eventClient.registerEvent(event)
            } catch (exception: Exception) {
                sdkLogger.error(
                    LogEntry(
                        "mobile-engage-session-end-request-failed",
                        mapOf("error" to (exception.message ?: "End session failed"))
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

    private fun createSessionEndEvent(): Event {
        val sessionEnd = timestampProvider.provide()
        val duration =
            sessionEnd.toEpochMilliseconds() - sessionContext.sessionStart!!
        return Event.createSessionEnd(duration, sessionEnd)
    }


}