package com.sap.ec.mobileengage.push

import com.sap.ec.core.actions.ActionHandlerApi
import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.PushActionFactoryApi
import com.sap.ec.mobileengage.action.actions.Action
import com.sap.ec.mobileengage.action.models.ActionModel
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.sap.ec.mobileengage.action.models.NotificationOpenedActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.mobileengage.push.model.JsNotificationClickedData
import com.sap.ec.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import web.broadcast.BroadcastChannel
import web.events.EventHandler

internal class PushNotificationClickHandler(
    private val actionFactory: PushActionFactoryApi,
    private val actionHandler: ActionHandlerApi,
    private val onNotificationClickedBroadcastChannel: BroadcastChannel,
    private val coroutineScope: CoroutineScope,
    private val sdkLogger: Logger
) : PushNotificationClickHandlerApi {

    override suspend fun register() {
        onNotificationClickedBroadcastChannel.onmessage = EventHandler { event ->
            coroutineScope.launch {
                handleNotificationClick(event.data.unsafeCast<String>())
            }
        }
    }

    internal suspend fun handleNotificationClick(event: String) {
        try {
            val jsNotificationClickedData =
                JsonUtil.json.decodeFromString<JsNotificationClickedData>(event)

            val actionModel = if (triggeredByDefaultTap(jsNotificationClickedData)) {
                jsNotificationClickedData.jsPushMessage.actionableData?.defaultTapAction
            } else {
                findAction(
                    jsNotificationClickedData.actionId,
                    jsNotificationClickedData.jsPushMessage.actionableData?.actions
                )
            }
            val triggeredAction = actionModel?.let {
                actionFactory.create(it)
            }
            val mandatoryActions = createMandatoryActions(jsNotificationClickedData, actionModel)
            actionHandler.handleActions(mandatoryActions, triggeredAction)

        } catch (e: Exception) {
            sdkLogger.error("PushNotificationClickHandler - handleNotificationClick", e)
        }
    }

    private suspend fun createMandatoryActions(
        jsNotificationClickedData: JsNotificationClickedData,
        actionModel: ActionModel?
    ): List<Action<*>> {
        return when (actionModel) {

            is PresentableActionModel -> {
                val model = BasicPushButtonClickedActionModel(
                    actionModel.reporting,
                    jsNotificationClickedData.jsPushMessage.trackingInfo
                )
                listOf(actionFactory.create(model))
            }

            is BasicActionModel,
            null -> {
                listOf(actionFactory.create(NotificationOpenedActionModel(actionModel?.reporting, jsNotificationClickedData.jsPushMessage.trackingInfo)))
            }

            else -> emptyList()
        }
    }

    private fun triggeredByDefaultTap(jsNotificationClickedData: JsNotificationClickedData) =
        jsNotificationClickedData.actionId.isEmpty()

    private fun findAction(
        actionId: String,
        actionModels: List<PresentableActionModel>?
    ): ActionModel? {
        return actionModels
            ?.find { it.id == actionId }
    }
}