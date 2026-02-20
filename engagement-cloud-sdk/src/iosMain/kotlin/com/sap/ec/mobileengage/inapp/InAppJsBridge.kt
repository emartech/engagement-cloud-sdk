package com.sap.ec.mobileengage.inapp

import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.ActionFactoryApi
import com.sap.ec.mobileengage.action.models.ActionModel
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.amendForJsBridge
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import com.sap.ec.util.JsonUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import platform.Foundation.NSDictionary
import platform.Foundation.allKeys
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.darwin.NSObject

class InAppJsBridge(
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val inAppJsBridgeData: InAppJsBridgeData,
    private val mainDispatcher: CoroutineDispatcher,
    private val sdkDispatcher: CoroutineDispatcher,
    private val logger: Logger,
    private val json: Json
) : NSObject(), WKScriptMessageHandlerProtocol {

    val scriptMessageHandler = "handleInAppAction"

    private fun handleActions(didReceiveScriptMessage: WKScriptMessage) {
        val sdkScope = CoroutineScope(sdkDispatcher)
        val mainScope = CoroutineScope(mainDispatcher)
        sdkScope.launch {
            try {
                val body: String = withContext(mainScope.coroutineContext) {
                    (didReceiveScriptMessage.body as NSDictionary).toJsonString()
                }
                logger.debug("Received action: body(${body})")
                val actionModel = json.decodeFromString<BasicActionModel>(body)
                    .amendForJsBridge(inAppJsBridgeData)
                actionFactory.create(actionModel).invoke()
            } catch (error: Throwable) {
                logger.error("Failed to parse actionModel from inapp action data.", error)
            }
        }
    }

    fun registerContentController(): WKUserContentController {
        val userContentController = WKUserContentController()
        userContentController.addScriptMessageHandler(this, scriptMessageHandler)
        return userContentController
    }

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage
    ) {
        handleActions(didReceiveScriptMessage)
    }

    private fun NSDictionary.toMap(): Map<String, String?> {
        val map = mutableMapOf<String, String?>()
        val keys = this.allKeys
        for (key in keys) {
            val keyString = key as? String ?: continue
            val value = this.objectForKey(keyString)
            map[keyString] = value.toString()
        }
        return map
    }

    private fun NSDictionary.toJsonString(): String {
        val map = this.toMap()
        return convertMapToJsonString(map)
    }

    private fun convertMapToJsonString(map: Map<String, String?>): String {
        return JsonUtil.json.encodeToString(map)
    }
}
