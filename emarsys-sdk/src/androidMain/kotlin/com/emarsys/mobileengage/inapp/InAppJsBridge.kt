package com.emarsys.mobileengage.inapp

import android.webkit.JavascriptInterface
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicCopyToClipboardActionModel
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import com.emarsys.mobileengage.action.models.BasicDismissActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.RequestPushPermissionActionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class InAppJsBridge(
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val json: Json,
    private val sdkScope: CoroutineScope
) {


    @JavascriptInterface
    fun triggerMEEvent(jsonString: String) {
        sdkScope.launch {
            val actionModel = json.decodeFromString<BasicCustomEventActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun buttonClicked(jsonString: String) {
        sdkScope.launch {
            val actionModel =
                json.decodeFromString<BasicButtonClickedActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun triggerAppEvent(jsonString: String) {
        sdkScope.launch {
            val actionModel = json.decodeFromString<BasicAppEventActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun requestPushPermission(jsonString: String) {
        sdkScope.launch {
            val actionModel =
                json.decodeFromString<RequestPushPermissionActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun openExternalLink(jsonString: String) {
        sdkScope.launch {
            val actionModel =
                json.decodeFromString<BasicOpenExternalUrlActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun close(jsonString: String) {
        sdkScope.launch {
            val actionModel = json.decodeFromString<BasicDismissActionModel>(jsonString)
            actionModel.topic = "dismiss"
            actionFactory.create(actionModel)()
        }
    }

    @JavascriptInterface
    fun copyToClipboard(jsonString: String) {
        sdkScope.launch {
            val actionModel =
                json.decodeFromString<BasicCopyToClipboardActionModel>(jsonString)
            actionFactory.create(actionModel)()
        }
    }
}

