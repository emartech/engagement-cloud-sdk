package com.emarsys.reregistration.states

import com.emarsys.core.log.Logger
import com.emarsys.core.session.SessionContext
import com.emarsys.core.state.State

class ClearSessionContextState(
    private val sessionContext: SessionContext,
    val sdkLogger: Logger
) : State {

    override val name = "clearSessionContextState"

    override fun prepare() {}

    override suspend fun active() {
        sdkLogger.debug("Clearing session tokens")
        sessionContext.clearSessionTokens()
    }

    override fun relax() {}

}