package com.sap.ec.init.states

import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.mobileengage.session.SessionApi
import com.sap.ec.watchdog.lifecycle.LifecycleWatchDog

internal class SessionSubscriptionState(
    private val ecSdkSession: SessionApi,
    private val lifecycleWatchDog: LifecycleWatchDog,
    private val sdkLogger: Logger

) : State {
    override val name: String = "sessionSubscriptionState"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Subscribing to session")

        ecSdkSession.subscribe(lifecycleWatchDog)
        return Result.success(Unit)
    }

    override fun relax() {
    }
}