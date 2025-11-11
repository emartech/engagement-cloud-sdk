package com.emarsys.mobileengage.pushtoinapp

import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.log.Logger
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
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