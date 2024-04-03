package com.emarsys.mobileengage.session

import com.emarsys.api.SdkResult
import com.emarsys.core.actions.LifecycleEvent
import com.emarsys.core.providers.Provider
import com.emarsys.core.session.SessionContext
import com.emarsys.networking.clients.event.EventClientApi
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
        return SdkResult.Success(null)
    }

    override suspend fun endSession(): SdkResult {
        sessionContext.sessionStart = null
        return SdkResult.Success(null)
    }
}