package com.sap.ec.mobileengage.pushtoinapp

import com.sap.ec.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.inapp.networking.download.InAppDownloaderApi
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)

internal class PushToInAppHandler(
    private val downloader: InAppDownloaderApi,
    private val sdkLogger: Logger,
    private val sdkEventManager: SdkEventManagerApi

) : PushToInAppHandlerApi {
    override suspend fun handle(url: String) {
        val inAppMessage = downloader.download(url)
        if (!inAppMessage?.content.isNullOrEmpty()) {
            sdkLogger.debug("Handling push-to-inApp action")
            sdkEventManager.emitEvent(
                SdkEvent.Internal.InApp.Present(
                    inAppMessage = inAppMessage
                )
            )
        }
    }
}