package com.emarsys.reregistration.states

import com.emarsys.core.session.SessionContextApi
import com.emarsys.core.state.State

class ClearSessionContextState(private val sessionContext: SessionContextApi) : State {

    override val name = "clearSessionContextState"

    override fun prepare() {}

    override suspend fun active() {
        sessionContext.clearSessionTokens()
    }

    override fun relax() {}

}