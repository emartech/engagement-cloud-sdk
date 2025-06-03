package com.emarsys.init.states

import com.emarsys.core.log.Logger
import com.emarsys.core.state.State
import com.emarsys.mobileengage.session.Session
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog

internal class SessionSubscriptionState(
    private val emarsysSdkSession: Session,
    private val lifecycleWatchDog: LifecycleWatchDog,
    private val sdkLogger: Logger

) : State {
    override val name: String = "sessionSubscriptionState"

    override fun prepare() {
    }

    override suspend fun active() {
        sdkLogger.debug("Subscribing to session")

        emarsysSdkSession.subscribe(lifecycleWatchDog)
    }

    override fun relax() {
    }
}