package com.sap.ec.mobileengage.inapp

import android.webkit.JavascriptInterface
import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.EventActionFactoryApi
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.BasicAppEventActionModel
import com.sap.ec.mobileengage.action.models.BasicCopyToClipboardActionModel
import com.sap.ec.mobileengage.action.models.BasicCustomEventActionModel
import com.sap.ec.mobileengage.action.models.BasicDismissActionModel
import com.sap.ec.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.sap.ec.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.sap.ec.mobileengage.action.models.RequestPushPermissionActionModel
import com.sap.ec.mobileengage.action.models.amendForJsBridge
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

internal class InAppJsBridge(
    private val inAppJsBridgeData: InAppJsBridgeData,
    private val actionFactory: EventActionFactoryApi,
    private val applicationScope: CoroutineScope,
    private val json: Json,
    private val logger: Logger
) {
    private val actionChannel = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    init {
        applicationScope.launch {
            for (action in actionChannel) {
                action()
            }
        }
    }

    @JavascriptInterface
    fun handleInAppAction(jsonString: String) {
        try {
            val actionModel =
                json.decodeFromString<BasicActionModel>(jsonString)
                    .amendForJsBridge(inAppJsBridgeData)
            actionChannel.trySend {
                actionFactory.create(actionModel).invoke()
            }
        } catch (error: Throwable) {
            applicationScope.launch {
                logger.error("Failed to parse actionModel from inapp action data.", error)
            }
        }
    }

    @JavascriptInterface
    fun triggerMEEvent(jsonString: String) {
        actionChannel.trySend {
            val actionModel = json.decodeFromString<BasicCustomEventActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun buttonClicked(jsonString: String) {
        actionChannel.trySend {
            val buttonClickJson = json.decodeFromString<JsonObject>(jsonString)
            val reporting: String = buttonClickJson["reporting"]?.jsonPrimitive?.contentOrNull ?: ""
            val actionModel =
                BasicInAppButtonClickedActionModel(
                    reporting,
                    inAppJsBridgeData.trackingInfo,
                    inAppJsBridgeData.inAppType
                )
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun triggerAppEvent(jsonString: String) {
        actionChannel.trySend {
            val actionModel = json.decodeFromString<BasicAppEventActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun requestPushPermission(jsonString: String) {
        actionChannel.trySend {
            val actionModel =
                json.decodeFromString<RequestPushPermissionActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun openExternalLink(jsonString: String) {
        actionChannel.trySend {
            val actionModel =
                json.decodeFromString<BasicOpenExternalUrlActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun close(jsonString: String) {
        actionChannel.trySend {
            val actionModel =
                json.decodeFromString<BasicDismissActionModel>(jsonString)
                    .amendForJsBridge(inAppJsBridgeData)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun copyToClipboard(jsonString: String) {
        actionChannel.trySend {
            val actionModel =
                json.decodeFromString<BasicCopyToClipboardActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }
}
