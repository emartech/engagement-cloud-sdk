package com.emarsys.mobileengage.pushtoinapp

import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi

class PushToInAppHandler(
    private val downloader: InAppDownloaderApi,
    private val inAppHandler: InAppHandlerApi,
    private val sdkLogger: Logger
) : PushToInAppHandlerApi {
    override suspend fun handle(actionModel: InternalPushToInappActionModel) {
        sdkLogger.debug("Handling push to in-app action")
        val html = actionModel.html ?: downloader.download(actionModel.url)
        val campaignId = actionModel.campaignId
        if (!html.isNullOrEmpty()) {
            inAppHandler.handle(campaignId, html)
        }
    }
}