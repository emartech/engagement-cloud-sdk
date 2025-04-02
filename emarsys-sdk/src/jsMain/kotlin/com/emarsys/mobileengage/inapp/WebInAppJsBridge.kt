package com.emarsys.mobileengage.inapp

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
import web.window.window

internal class WebInAppJsBridge(
    private val actionFactory: EventActionFactoryApi,
    private val json: Json,
    private val sdkDispatcher: CoroutineDispatcher,
    private val campaignId: String
) : InAppJsBridgeApi {

    override fun register() {
        window.asDynamic()["EMSInappWebBridge"] = EMSInappWebBridge()
    }

    inner class EMSInappWebBridge {

        @JsName("triggerMEEvent")
        fun triggerMEEvent(jsonString: String) {
            CoroutineScope(sdkDispatcher).launch {
                val actionModel = json.decodeFromString<BasicCustomEventActionModel>(jsonString)
                actionFactory.create(actionModel)()
            }
        }

        @JsName("buttonClicked")
        fun buttonClicked(jsonString: String) {
            CoroutineScope(sdkDispatcher).launch {
                val actionModel =
                    json.decodeFromString<BasicInAppButtonClickedActionModel>(jsonString)
                actionFactory.create(actionModel)()
            }
        }

        @JsName("triggerAppEvent")
        fun triggerAppEvent(jsonString: String) {
            CoroutineScope(sdkDispatcher).launch {
                val actionModel = json.decodeFromString<BasicAppEventActionModel>(jsonString)
                actionFactory.create(actionModel)()
            }
        }

        @JsName("requestPushPermission")
        fun requestPushPermission(jsonString: String) {
            CoroutineScope(sdkDispatcher).launch {
                val actionModel =
                    json.decodeFromString<RequestPushPermissionActionModel>(jsonString)
                actionFactory.create(actionModel)()
            }
        }

        @JsName("openExternalLink")
        fun openExternalLink(jsonString: String) {
            CoroutineScope(sdkDispatcher).launch {
                val actionModel = json.decodeFromString<BasicOpenExternalUrlActionModel>(jsonString)
                actionFactory.create(actionModel)()
            }
        }

        @JsName("close")
        fun dismiss(jsonString: String) {
            CoroutineScope(sdkDispatcher).launch {
                val actionModel = json.decodeFromString<BasicDismissActionModel>(jsonString)
                actionModel.dismissId = campaignId
                actionFactory.create(actionModel)()
            }
        }

        @JsName("copyToClipboard")
        fun copyToClipboard(jsonString: String) {
            CoroutineScope(sdkDispatcher).launch {
                val actionModel = json.decodeFromString<BasicCopyToClipboardActionModel>(jsonString)
                actionFactory.create(actionModel)()
            }
        }
    }
}

