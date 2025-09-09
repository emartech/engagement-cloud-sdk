package com.emarsys.networking.clients.embedded.messaging

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.log.Logger
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.embedded.messages.EmbeddedMessagingRequestFactoryApi
import com.emarsys.networking.clients.EventBasedClientApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal class EmbeddedMessagingClient(
    private val sdkLogger: Logger,
    private val sdkEventManager: SdkEventManagerApi,
    private val applicationScope: CoroutineScope,
    private val embeddedMessagingRequestFactory: EmbeddedMessagingRequestFactoryApi
) : EventBasedClientApi {
    override suspend fun register() {

        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkLogger.debug("EmbeddedMessagingClient - register")
            sdkEventManager.onlineSdkEvents
                .filter { it is SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount }
                .collect {
                embeddedMessagingRequestFactory.create(it as SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount)
            }
        }

    }

}