package com.emarsys.mobileengage.inapp

import android.webkit.JavascriptInterface
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicCopyToClipboardActionModel
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import com.emarsys.mobileengage.action.models.BasicDismissActionModel
import com.emarsys.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.RequestPushPermissionActionModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

internal class InAppJsBridge(
    private val inAppJsBridgeData: InAppJsBridgeData,
    private val actionFactory: EventActionFactoryApi,
    private val sdkDispatcher: CoroutineDispatcher,
    private val json: Json
) {


    @JavascriptInterface
    fun triggerMEEvent(jsonString: String) {
        CoroutineScope(sdkDispatcher).launch {
            val actionModel = json.decodeFromString<BasicCustomEventActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun buttonClicked(jsonString: String) {
        CoroutineScope(sdkDispatcher).launch {
            val buttonClickJson = json.decodeFromString<JsonObject>(jsonString)
            val reporting = buttonClickJson["reporting"]?.jsonPrimitive?.contentOrNull
            reporting?.let {
                val actionModel =
                    BasicInAppButtonClickedActionModel(it, inAppJsBridgeData.trackingInfo)
                actionFactory.create(actionModel)()
            }
        }
    }

    @JavascriptInterface
    fun triggerAppEvent(jsonString: String) {
        CoroutineScope(sdkDispatcher).launch {
            val actionModel = json.decodeFromString<BasicAppEventActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun requestPushPermission(jsonString: String) {
        CoroutineScope(sdkDispatcher).launch {
            val actionModel =
                json.decodeFromString<RequestPushPermissionActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun openExternalLink(jsonString: String) {
        CoroutineScope(sdkDispatcher).launch {
            val actionModel =
                json.decodeFromString<BasicOpenExternalUrlActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun close(jsonString: String) {
        CoroutineScope(sdkDispatcher).launch {
            val actionModel = json.decodeFromString<BasicDismissActionModel>(jsonString)
            actionModel.dismissId = inAppJsBridgeData.dismissId
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun copyToClipboard(jsonString: String) {
        CoroutineScope(sdkDispatcher).launch {
            val actionModel =
                json.decodeFromString<BasicCopyToClipboardActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }
}

