package com.emarsys.mobileengage.pushtoinapp

import com.emarsys.core.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi

class WebPushToInAppHandler(
    private val downloader: InAppDownloaderApi,
    private val inAppHandler: InAppHandlerApi
): PushToInAppHandlerApi {
    override suspend fun handle(actionModel: InternalPushToInappActionModel) {
        val html = actionModel.html ?: downloader.download(actionModel.url)

        presentInApp(html)
    }

    private suspend fun presentInApp(html: String?) {
        if (!html.isNullOrEmpty()) {
            inAppHandler.handle(html)
        }
    }
}
