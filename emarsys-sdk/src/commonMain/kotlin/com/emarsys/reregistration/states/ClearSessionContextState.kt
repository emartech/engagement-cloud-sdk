package com.emarsys.reregistration.states

import com.emarsys.core.log.Logger
import com.emarsys.core.networking.context.RequestContext
import com.emarsys.core.state.State

internal class ClearSessionContextState(
    private val requestContext: RequestContext,
    val sdkLogger: Logger
) : State {

    override val name = "clearSessionContextState"

    override fun prepare() {}

    override suspend fun active() {
        sdkLogger.debug("Clearing session tokens")
        requestContext.clearTokens()
    }

    override fun relax() {}

}