package com.emarsys.mobileengage.pushtoinapp

import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.models.PresentablePushToInAppActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.InAppType

class PushToInAppHandler(
    private val downloader: InAppDownloaderApi,
    private val inAppHandler: InAppHandlerApi,
    private val sdkLogger: Logger
) : PushToInAppHandlerApi {
    override suspend fun handle(actionModel: PresentablePushToInAppActionModel) {
        sdkLogger.debug("Handling push to in-app action")
        val content = downloader.download(actionModel.payload.url)
        if (!content.isNullOrEmpty()) {
            actionModel.trackingInfo?.let {
                inAppHandler.handle(
                    InAppMessage(
                        dismissId = actionModel.id,
                        type = InAppType.OVERLAY,
                        trackingInfo = it,
                        content = content
                    )
                )
            }
        }
    }
}