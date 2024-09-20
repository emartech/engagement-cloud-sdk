package com.emarsys.mobileengage.push

import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.push.model.JsNotificationClickedData
import com.emarsys.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import web.broadcast.BroadcastChannel

class PushNotificationClickHandler(
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val onNotificationClickedBroadcastChannel: BroadcastChannel,
    private val coroutineScope: CoroutineScope,
    private val sdkLogger: Logger
) : PushNotificationClickHandlerApi {

    override fun register() {
        onNotificationClickedBroadcastChannel.onmessage = { event ->
            coroutineScope.launch {
                handleNotificationClick(event.data.unsafeCast<String>())
            }
        }
    }

    internal suspend fun handleNotificationClick(event: String) {
        try {
            val jsNotificationClickedData =
                JsonUtil.json.decodeFromString<JsNotificationClickedData>(event)

            if (hasDefaultTapActionId(jsNotificationClickedData)) {
                invokeDefaultTapAction(jsNotificationClickedData.jsPushMessage.data.defaultTapAction)
            } else {
                findAndInvokeAction(
                    jsNotificationClickedData.actionId,
                    jsNotificationClickedData.jsPushMessage.data.actions
                )
            }
        } catch (e: Exception) {
            sdkLogger.error("PushNotificationClickHandler - handleNotificationClick", e)
        }
    }

    private fun hasDefaultTapActionId(jsNotificationClickedData: JsNotificationClickedData) =
        jsNotificationClickedData.actionId.isEmpty()

    private suspend fun invokeDefaultTapAction(defaultTapAction: BasicActionModel?) {
        defaultTapAction?.let { action ->
            actionFactory.create(action).invoke()
        }
    }

    private suspend fun findAndInvokeAction(
        actionId: String,
        actionModels: List<PresentableActionModel>?
    ) {
        actionModels
            ?.find { it.id == actionId }
            ?.let { action ->
                actionFactory.create(action).invoke()
            }
    }
}