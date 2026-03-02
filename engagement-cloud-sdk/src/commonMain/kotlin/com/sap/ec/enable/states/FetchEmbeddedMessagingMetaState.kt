package com.sap.ec.enable.states

import com.sap.ec.context.Features
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.state.State
import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)

internal class FetchEmbeddedMessagingMetaState(
    private val sdkContext: SdkContextApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkLogger: Logger
) : State {
    override val name: String = "fetchEmbeddedMessagingMetaState"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Fetching Embedded Messaging Meta State started")
        if (sdkContext.features.contains(Features.EmbeddedMessaging)) {
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.EmbeddedMessaging.FetchMeta()
            ).await<Response>()
                .result
                .onFailure {
                    sdkLogger.error(
                        "Failed to fetch Embedded Messaging Meta data, proceeding anyway",
                        it,
                        isRemoteLog = false
                    )
                }
        } else {
            sdkLogger.debug("Feature Embedded Messaging is disabled, skipping Fetch Meta data job")
        }
        return Result.success(Unit)
    }

    override fun relax() {
    }
}