package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.darwin.NSObject

class InAppJsBridge(
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val json: Json,
    private val sdkScope: CoroutineScope
) : NSObject(), WKScriptMessageHandlerProtocol {

    private val jsCommandNames = listOf(
        "triggerMEEvent",
        "buttonClicked",
        "triggerAppEvent",
        "requestPushPermission",
        "openExternalLink",
        "close",
        "copyToClipboard"
    )

    private fun handleActions(didReceiveScriptMessage: WKScriptMessage) {
        sdkScope.launch {
            val actionModel =
                json.decodeFromString<BasicCustomEventActionModel>(didReceiveScriptMessage.body.toString())
            actionFactory.create(actionModel)()
        }
    }

    fun registerContentController(): WKUserContentController {
        val userContentController = WKUserContentController()
        jsCommandNames.forEach { jsCommandName ->
            userContentController.addScriptMessageHandler(this, jsCommandName)
        }
        return userContentController
    }

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage
    ) {
        handleActions(didReceiveScriptMessage)
    }

}
