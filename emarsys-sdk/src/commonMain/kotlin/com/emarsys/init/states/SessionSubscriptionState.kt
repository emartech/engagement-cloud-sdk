package com.emarsys.init.states

import com.emarsys.core.state.State
import com.emarsys.mobileengage.session.Session
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog

class SessionSubscriptionState(
    private val mobileEngageSession: Session,
    private val lifecycleWatchDog: LifecycleWatchDog
) : State {
    override val name: String = "sessionSubscriptionState"

    override fun prepare() {
    }

    override suspend fun active() {
        mobileEngageSession.subscribe(lifecycleWatchDog)
    }

    override fun relax() {
    }
}