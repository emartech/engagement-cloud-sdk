package com.sap.ec.reregistration.states

import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.state.State

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