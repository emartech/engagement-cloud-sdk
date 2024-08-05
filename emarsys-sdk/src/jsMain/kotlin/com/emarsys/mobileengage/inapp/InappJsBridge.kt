package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import web.window.window

class InappJsBridge(
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val sdkDispatcher: CoroutineDispatcher,
    private val json: Json,
    private val sdkScope: CoroutineScope
) : InappJsBridgeApi {

    override fun register() {
        window.asDynamic()["EMSInappWebBridge"] = EMSInappWebBridge()
    }

    inner class EMSInappWebBridge {

        @JsName("triggerMEEvent")
        fun triggerMEEvent(jsonString: String) {
            println("custom event triggered")
            CoroutineScope(sdkDispatcher).launch {
                val actionModel = json.decodeFromString<BasicCustomEventActionModel>(jsonString)
                actionFactory.create(actionModel)()
            }
        }

        @JsName("buttonClicked")
        fun buttonClicked(jsonString: String) {
            sdkScope.launch {
                val actionModel = json.decodeFromString<BasicButtonClickedActionModel>(jsonString)
                actionFactory.create(actionModel)()
            }
        }
    }
}

