package com.sap.ec.core.url

import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.models.OpenExternalUrlActionModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosExternalUrlOpener(
    private val uiApplication: UIApplication,
    private val mainDispatcher: CoroutineDispatcher,
    private val sdkDispatcher: CoroutineDispatcher,
    private val sdkLogger: Logger
) : ExternalUrlOpenerApi {

    override suspend fun open(actionModel: OpenExternalUrlActionModel) {
        val nsUrl = NSURL(string = actionModel.url)
        if (uiApplication.canOpenURL(nsUrl)) {
            withContext(mainDispatcher) {
                uiApplication.openURL(nsUrl, emptyMap<Any?, Any?>()) { success ->
                    if (!success) {
                        CoroutineScope(sdkDispatcher).launch {
                            sdkLogger.error(
                                "IosExternalUrlOpener",
                                buildJsonObject {
                                    put(
                                        "message",
                                        JsonPrimitive("Failed to open url: ${actionModel.url}")
                                    )
                                }

                            )
                        }
                    }
                }
            }
        }
    }

}