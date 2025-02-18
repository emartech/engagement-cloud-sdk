package com.emarsys.mobileengage.inapp

import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicCopyToClipboardActionModel
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import com.emarsys.mobileengage.action.models.BasicDismissActionModel
import com.emarsys.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.RequestPushPermissionActionModel
import com.emarsys.util.JsonUtil
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
    private val json: Json,
    private val mainScope: CoroutineScope,
    private val sdkScope: CoroutineScope,
    private val logger: Logger
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
            val body: String = withContext(mainScope.coroutineContext) {
                (didReceiveScriptMessage.body as NSDictionary).toJsonString()
            }
            val name = withContext(mainScope.coroutineContext) {
                didReceiveScriptMessage.name
            }
            logger.debug(
                "JsBridge",
                "Received action: name(${name}), body(${body})"
            )
            when (name) {
                "triggerMEEvent" -> {
                    val actionModel =
                        json.decodeFromString<BasicCustomEventActionModel>(body)
                    actionFactory.create(actionModel)()
                }

                "buttonClicked" -> {
                    val actionModel =
                        json.decodeFromString<BasicInAppButtonClickedActionModel>(body)
                    actionFactory.create(actionModel)()
                }

                "triggerAppEvent" -> {
                    val actionModel =
                        json.decodeFromString<BasicAppEventActionModel>(body)
                    actionFactory.create(actionModel)()
                }

                "requestPushPermission" -> {
                    val actionModel =
                        json.decodeFromString<RequestPushPermissionActionModel>(
                            body
                        )
                    actionFactory.create(actionModel)()
                }

                "openExternalLink" -> {
                    val actionModel =
                        json.decodeFromString<BasicOpenExternalUrlActionModel>(
                            body
                        )
                    actionFactory.create(actionModel)()
                }

                "close" -> {
                    val actionModel =
                        json.decodeFromString<BasicDismissActionModel>(body)
                    actionModel.campaignId = "dismiss"
                    actionFactory.create(actionModel)()
                }

                "copyToClipboard" -> {
                    val actionModel =
                        json.decodeFromString<BasicCopyToClipboardActionModel>(
                            body
                        )
                    actionFactory.create(actionModel)()
                }

                else -> {
                    logger.info("JsBridge", "Unknown action: ${didReceiveScriptMessage.name}")
                }
            }
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

    private fun NSDictionary.toMap(): Map<String, String?>
    {
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
