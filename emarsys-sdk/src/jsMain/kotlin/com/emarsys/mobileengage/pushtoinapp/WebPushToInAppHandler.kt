package com.emarsys.mobileengage.pushtoinapp

import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.mobileengage.action.models.PresentablePushToInAppActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi

class WebPushToInAppHandler(
    private val downloader: InAppDownloaderApi,
    private val inAppHandler: InAppHandlerApi
) : PushToInAppHandlerApi {
    override suspend fun handle(actionModel: PresentablePushToInAppActionModel) {
        val html = downloader.download(actionModel.payload.url)
        val campaignId = actionModel.payload.campaignId
        if (!html.isNullOrEmpty()) {
            inAppHandler.handle(campaignId, html)
        }
    }
}
