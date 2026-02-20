package com.sap.ec.mobileengage.inapp

import android.webkit.JavascriptInterface
import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.EventActionFactoryApi
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.amendForJsBridge
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

internal class InAppJsBridge(
    private val inAppJsBridgeData: InAppJsBridgeData,
    private val actionFactory: EventActionFactoryApi,
    private val applicationScope: CoroutineScope,
    private val json: Json,
    private val logger: Logger
) {
    @JavascriptInterface
    fun handleInAppAction(jsonString: String) {
        try {
            val actionModel =
                json.decodeFromString<BasicActionModel>(jsonString).amendForJsBridge(inAppJsBridgeData)
            applicationScope.launch {
                actionFactory.create(actionModel).invoke()
            }
        } catch (error: Throwable) {
            applicationScope.launch {
                logger.error("Failed to parse actionModel from inapp action data.", error)
            }
        }
    }
}

