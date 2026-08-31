package com.sap.ec.networking.clients.recommendation

import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.networking.clients.EventBasedClientApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

internal class RecommendationClient(
    private val sdkEventManager: SdkEventManagerApi,
    private val applicationScope: CoroutineScope,
    private val sdkLogger: Logger,
) : EventBasedClientApi {

    override suspend fun register() {
        sdkLogger.debug("register RecommendationClient")
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkEventManager.onlineSdkEvents.filterIsInstance<SdkEvent.External.WebExtendEvent>()
                .collect {
                    sdkLogger.debug("consume RecommendationClient events")
                }
        }
    }
}