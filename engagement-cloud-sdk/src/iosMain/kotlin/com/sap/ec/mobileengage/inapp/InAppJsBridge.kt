package com.sap.ec.mobileengage.inapp

import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.ActionFactoryApi
import com.sap.ec.mobileengage.action.models.ActionModel
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonArray
import platform.Foundation.NSArray
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

    private val jsCommandNames = listOf(
        "triggerMEEvent",
        "buttonClicked",
        "triggerAppEvent",
        "requestPushPermission",
        "openExternalLink",
        "close",
        "copyToClipboard",
        "handleInAppAction"
    )

    private fun handleActions(didReceiveScriptMessage: WKScriptMessage) {
        val sdkScope = CoroutineScope(sdkDispatcher)
        val mainScope = CoroutineScope(mainDispatcher)
        sdkScope.launch {
            try {
                val body: String = withContext(mainScope.coroutineContext) {
                    didReceiveScriptMessage.body.toJsonString()
                }
                val name = withContext(mainScope.coroutineContext) {
                    didReceiveScriptMessage.name
                }
                logger.debug("Received action: body(${body})")
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
                        actionModel.dismissId = inAppJsBridgeData.dismissId
                        actionFactory.create(actionModel)()
                    }

                    "copyToClipboard" -> {
                        val actionModel =
                            json.decodeFromString<BasicCopyToClipboardActionModel>(
                                body
                            )
                        actionFactory.create(actionModel)()
                    }

                    "handleInAppAction" -> {
                        val actionModel = json.decodeFromString<BasicActionModel>(body)
                            .amendForJsBridge(inAppJsBridgeData)
                        actionFactory.create(actionModel).invoke()
                    }

                    else -> {
                        logger.info("Unknown action: ${didReceiveScriptMessage.name}")
                    }
                }
            } catch (error: Throwable) {
                logger.error("Failed to parse actionModel from inapp action data.", error)
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
    private fun Any.toJsonString(): String {
        val jsonElement = this.toJsonElement()
        return json.encodeToString(JsonElement.serializer(), jsonElement)
    }

    @Suppress("UNCHECKED_CAST")
    private fun Any?.toJsonElement(): JsonElement {
        return when (this) {
            null -> JsonNull
            is Map<*, *> -> {
                buildJsonObject {
                    this@toJsonElement.forEach { (key, value) ->
                        val keyString = key.toString()
                        put(keyString, value.toJsonElement())
                    }
                }
            }
            is List<*> -> {
                JsonArray(this.map { it.toJsonElement() })
            }
            is String -> JsonPrimitive(this)
            is Number -> JsonPrimitive(this)
            is Boolean -> JsonPrimitive(this)
            else -> JsonPrimitive(this.toString())
        }
    }
}
