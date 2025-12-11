package com.emarsys.enable.states

import com.emarsys.core.log.Logger
import com.emarsys.core.state.State
import com.emarsys.mobileengage.embeddedmessaging.pagination.EmbeddedMessagingPaginationHandlerApi
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)

internal class EmbeddedMessagingPaginationHandlerSetupState(
    private val embeddedMessagingPaginationHandler: EmbeddedMessagingPaginationHandlerApi,
    private val sdkLogger: Logger
) : State {
    override val name: String = "embeddedMessagingPaginationHandlerSetupState"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug(" Embedded Messaging Pagination Handler Setup State started")
        embeddedMessagingPaginationHandler.register()
        return Result.success(Unit)
    }

    override fun relax() {
    }
}