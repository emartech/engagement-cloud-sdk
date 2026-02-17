package com.sap.ec.mobileengage.inapp

import com.sap.ec.mobileengage.action.EventActionFactoryApi
import com.sap.ec.mobileengage.action.models.BasicAppEventActionModel
import com.sap.ec.mobileengage.action.models.BasicCopyToClipboardActionModel
import com.sap.ec.mobileengage.action.models.BasicCustomEventActionModel
import com.sap.ec.mobileengage.action.models.BasicDismissActionModel
import com.sap.ec.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.sap.ec.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.sap.ec.mobileengage.action.models.RequestPushPermissionActionModel
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import web.window.window

internal class WebInAppJsBridge(
    private val actionFactory: EventActionFactoryApi,
    private val inAppJsBridgeData: InAppJsBridgeData,
    private val sdkDispatcher: CoroutineDispatcher,
    private val json: Json
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
                val buttonClickJson = json.decodeFromString<JsonObject>(jsonString)
                val reporting = buttonClickJson["reporting"]?.jsonPrimitive?.contentOrNull
                reporting?.let {
                    val actionModel =
                        BasicInAppButtonClickedActionModel(it, inAppJsBridgeData.trackingInfo)
                    actionFactory.create(actionModel)()
                }
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
                actionModel.dismissId = inAppJsBridgeData.dismissId
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

