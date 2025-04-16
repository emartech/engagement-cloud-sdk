package com.emarsys.mobileengage.pushtoinapp

import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.mobileengage.action.models.PresentablePushToInAppActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi

class PushToInAppHandler(
    private val downloader: InAppDownloaderApi,
    private val inAppHandler: InAppHandlerApi
) : PushToInAppHandlerApi {
    override suspend fun handle(actionModel: PresentablePushToInAppActionModel) {
        val html = downloader.download(actionModel.payload.url)
        html?.let { inAppHandler.handle(actionModel.payload.campaignId, it) }
    }
}