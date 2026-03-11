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
import com.sap.ec.util.toJsonElement
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.darwin.NSObject

internal class InAppJsBridge(
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val inAppJsBridgeData: InAppJsBridgeData,
    private val mainDispatcher: CoroutineDispatcher,
    private val sdkDispatcher: CoroutineDispatcher,
    private val logger: Logger,
    private val json: Json
) : NSObject(), WKScriptMessageHandlerProtocol {

    private val actionChannel = Channel<WKScriptMessage>(Channel.UNLIMITED)

    init {
        CoroutineScope(sdkDispatcher).launch {
            for (message in actionChannel) {
                processAction(message)
            }
        }
    }

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

    private suspend fun processAction(didReceiveScriptMessage: WKScriptMessage) {
        val mainScope = CoroutineScope(mainDispatcher)
        try {
            val bodyElement: JsonElement = withContext(mainScope.coroutineContext) {
                didReceiveScriptMessage.body.toJsonElement()
            }
            val name = withContext(mainScope.coroutineContext) {
                didReceiveScriptMessage.name
            }
            logger.debug(
                "Received action: body(${
                    json.encodeToString(
                        JsonElement.serializer(),
                        bodyElement
                    )
                })"
            )
            when (name) {
                "triggerMEEvent" -> {
                    val actionModel =
                        json.decodeFromJsonElement(
                            BasicCustomEventActionModel.serializer(),
                            bodyElement
                        )
                    actionFactory.create(actionModel)()
                }

                "buttonClicked" -> {
                    val actionModel =
                        json.decodeFromJsonElement(
                            BasicInAppButtonClickedActionModel.serializer(),
                            bodyElement
                        ).amendForJsBridge(inAppJsBridgeData)
                    actionFactory.create(actionModel)()
                }

                "triggerAppEvent" -> {
                    val actionModel =
                        json.decodeFromJsonElement(
                            BasicAppEventActionModel.serializer(),
                            bodyElement
                        )
                    actionFactory.create(actionModel)()
                }

                "requestPushPermission" -> {
                    val actionModel =
                        json.decodeFromJsonElement(
                            RequestPushPermissionActionModel.serializer(),
                            bodyElement
                        )
                    actionFactory.create(actionModel)()
                }

                "openExternalLink" -> {
                    val actionModel =
                        json.decodeFromJsonElement(
                            BasicOpenExternalUrlActionModel.serializer(),
                            bodyElement
                        )
                    actionFactory.create(actionModel)()
                }

                "close" -> {
                    val actionModel =
                        json.decodeFromJsonElement(
                            BasicDismissActionModel.serializer(),
                            bodyElement
                        ).amendForJsBridge(inAppJsBridgeData)

                    actionFactory.create(actionModel)()
                }

                "copyToClipboard" -> {
                    val actionModel =
                        json.decodeFromJsonElement(
                            BasicCopyToClipboardActionModel.serializer(),
                            bodyElement
                        )
                    actionFactory.create(actionModel)()
                }

                "handleInAppAction" -> {
                    val actionModel =
                        json.decodeFromJsonElement(BasicActionModel.serializer(), bodyElement)
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
        actionChannel.trySend(didReceiveScriptMessage)
    }
}
