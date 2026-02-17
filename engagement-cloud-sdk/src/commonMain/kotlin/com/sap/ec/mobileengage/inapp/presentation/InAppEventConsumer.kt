package com.sap.ec.mobileengage.inapp.presentation

import com.sap.ec.core.Registerable
import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.inapp.view.InAppViewProviderApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal class InAppEventConsumer(
    private val applicationScope: CoroutineScope,
    private val sdkEventManager: SdkEventManagerApi,
    private val sdkLogger: Logger,
    private val inAppPresenter: InAppPresenterApi,
    private val inAppViewProvider: InAppViewProviderApi
) : Registerable {
    override suspend fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkLogger.debug("register InAppClient")
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventManager.sdkEventFlow
            .filter { it is SdkEvent.Internal.InApp.Present }
            .collect {
                val presentEvent = it as SdkEvent.Internal.InApp.Present
                val messageType = presentEvent.inAppMessage.type

                if (messageType == InAppType.INLINE || messageType.name == "INLINE") {
                    return@collect
                }

                val view = inAppViewProvider.provide()
                val webViewHolder = view.load(presentEvent.inAppMessage)
                inAppPresenter.present(view, webViewHolder, InAppPresentationMode.Overlay)
            }
    }
}