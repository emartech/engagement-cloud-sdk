package com.sap.ec.changeappcode.states

import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.state.State

internal class ClearLinkedContactState(
    private val requestContext: RequestContextApi,
    val sdkLogger: Logger
) : State {

    override val name = "clearLinkedContactState"

    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Clearing linked contact")
        requestContext.isContactLinked = false
        requestContext.linkedContactHash = null

        return Result.success(Unit)
    }

    override fun relax() {}

}