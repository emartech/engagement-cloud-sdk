package com.emarsys.init.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.body
import com.emarsys.core.state.State
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.networking.clients.embedded.messaging.model.MetaData

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
                    sdkLogger.error("Error happened while fetching Embedded Messaging Meta data", it)
                    Result.failure(it)
                }
            )
    }

    override fun relax() {
    }
}