package com.emarsys.mobileengage.pushtoinapp

import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.models.PresentablePushToInAppActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi

class PushToInAppHandler(
    private val downloader: InAppDownloaderApi,
    private val inAppHandler: InAppHandlerApi,
    private val sdkLogger: Logger
) : PushToInAppHandlerApi {
    override suspend fun handle(actionModel: PresentablePushToInAppActionModel) {
        val inAppMessage = downloader.download(actionModel.payload.url)
        if (!inAppMessage?.content.isNullOrEmpty()) {
            sdkLogger.debug("Handling push-to-inApp action")
            inAppHandler.handle(inAppMessage)
        }
    }
}