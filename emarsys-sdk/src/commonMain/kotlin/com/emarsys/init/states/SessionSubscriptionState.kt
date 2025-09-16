package com.emarsys.init.states

import com.emarsys.core.log.Logger
import com.emarsys.core.state.State
import com.emarsys.mobileengage.session.SessionApi
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog

internal class SessionSubscriptionState(
    private val emarsysSdkSession: SessionApi,
    private val lifecycleWatchDog: LifecycleWatchDog,
    private val sdkLogger: Logger

) : State {
    override val name: String = "sessionSubscriptionState"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Subscribing to session")

        emarsysSdkSession.subscribe(lifecycleWatchDog)
        return Result.success(Unit)
    }

    override fun relax() {
    }
}