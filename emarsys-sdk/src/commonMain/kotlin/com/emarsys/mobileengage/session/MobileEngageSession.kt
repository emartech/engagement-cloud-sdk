package com.emarsys.mobileengage.session

import com.emarsys.api.SdkResult
import com.emarsys.core.actions.LifecycleEvent
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
    private val eventClient: EventClientApi,
    private val sdkDispatcher: CoroutineDispatcher
) : Session {

    suspend fun subscribe(lifecycleWatchDog: LifecycleWatchDog) {
        CoroutineScope(sdkDispatcher).launch(
            start = CoroutineStart.UNDISPATCHED
        ) {
            lifecycleWatchDog.lifecycleEvents.collect { event ->
                when (event) {
                    LifecycleEvent.OnForeground -> {
                        startSession()
                    }

                    LifecycleEvent.OnBackground -> {
                        endSession()
                    }
                }
            }
        }
    }

    override suspend fun startSession(): SdkResult {

        sessionContext.sessionStart = timestampProvider.provide().toEpochMilliseconds()
        sessionContext.sessionId = SessionId(uuidProvider.provide())
        return try {
            eventClient.registerEvent(
                Event.createSessionStart(
                    timestampProvider.provide().toString()
                )
            )
            SdkResult.Success(null)
        } catch (e: Exception) {
            SdkResult.Failure(e)
        }
    }

    override suspend fun endSession(): SdkResult {
        return try {
            val duration =
                timestampProvider.provide().toEpochMilliseconds() - sessionContext.sessionStart!!
            eventClient.registerEvent(
                Event.createSessionEnd(
                    duration,
                    timestampProvider.provide().toString()
                )
            )
            sessionContext.sessionStart = null
            sessionContext.sessionId = null
            SdkResult.Success(null)
        } catch (e: Exception) {
            SdkResult.Failure(e)
        }
    }
}