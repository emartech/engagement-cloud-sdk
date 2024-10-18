package com.emarsys.mobileengage.push

import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.push.model.JsNotificationClickedData
import com.emarsys.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import web.broadcast.BroadcastChannel

class PushNotificationClickHandler(
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val actionHandler: ActionHandlerApi,
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

            val actionModel = if (hasDefaultTapActionId(jsNotificationClickedData)) {
                jsNotificationClickedData.jsPushMessage.data.defaultTapAction
            } else {
                findAction(
                    jsNotificationClickedData.actionId,
                    jsNotificationClickedData.jsPushMessage.data.actions
                )
            }
            actionModel?.let {
                val triggeredAction = actionFactory.create(it)
                val mandatoryActions = createMandatoryActions(jsNotificationClickedData, it)
                actionHandler.handleActions(mandatoryActions, triggeredAction)
            }
        } catch (e: Exception) {
            sdkLogger.error("PushNotificationClickHandler - handleNotificationClick", e)
        }
    }

    private suspend fun createMandatoryActions(
        jsNotificationClickedData: JsNotificationClickedData,
        actionModel: ActionModel
    ): List<Action<*>> {
        return when (actionModel) {

            is PresentableActionModel -> {
                val model = BasicPushButtonClickedActionModel(
                    actionModel.id,
                    jsNotificationClickedData.jsPushMessage.data.sid
                )
                listOf(actionFactory.create(model))
            }

            is BasicActionModel -> {
                listOf(actionFactory.create(NotificationOpenedActionModel(jsNotificationClickedData.jsPushMessage.data.sid)))
            }

            else -> emptyList()
        }
    }

    private fun hasDefaultTapActionId(jsNotificationClickedData: JsNotificationClickedData) =
        jsNotificationClickedData.actionId.isEmpty()

    private fun findAction(
        actionId: String,
        actionModels: List<PresentableActionModel>?
    ): ActionModel? {
        return actionModels
            ?.find { it.id == actionId }
    }
}