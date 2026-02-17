package com.sap.ec.enable.states

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.body
import com.sap.ec.core.state.State
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.sap.ec.networking.clients.embedded.messaging.model.MetaData
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)

internal class FetchEmbeddedMessagingMetaState(
    private val embeddedMessagingContext: EmbeddedMessagingContextApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkLogger: Logger
) : State {
    override val name: String = "fetchEmbeddedMessagingMetaState"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Fetching Embedded Messaging Meta State started")
        return sdkEventDistributor.registerEvent(
            SdkEvent.Internal.EmbeddedMessaging.FetchMeta()
        ).await<Response>()
            .result
            .fold(
                onSuccess = {
                    embeddedMessagingContext.metaData = it.body<MetaData>()
                    sdkLogger.debug("Meta data fetched and stored in EmbeddedMessagingContext")
                    Result.success(Unit)
                },
                onFailure = {
                    embeddedMessagingContext.metaData = null
                    sdkLogger.error("Error happened while fetching Embedded Messaging Meta data", it)
                    Result.failure(it)
                }
            )
    }

    override fun relax() {
    }
}