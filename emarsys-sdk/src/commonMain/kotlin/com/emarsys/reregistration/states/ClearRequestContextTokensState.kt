package com.emarsys.reregistration.states

import com.emarsys.core.log.Logger
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.state.State

internal class ClearRequestContextTokensState(
    private val requestContext: RequestContextApi,
    val sdkLogger: Logger
) : State {

    override val name = "clearRequestContextTokensState"

    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Clearing request context tokens")
        requestContext.clearTokens()

        return Result.success(Unit)
    }

    override fun relax() {}

}