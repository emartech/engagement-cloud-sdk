package com.sap.ec.changeappcode.states

import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.state.State

internal class ClearLinkedContactHashState(
    private val requestContext: RequestContextApi,
    val sdkLogger: Logger
) : State {

    override val name = "clearLinkedContactHashState"

    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Clearing linked contact hash")
        requestContext.linkedContactHash = null

        return Result.success(Unit)
    }

    override fun relax() {}

}