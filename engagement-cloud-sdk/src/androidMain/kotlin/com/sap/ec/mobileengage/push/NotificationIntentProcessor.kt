package com.sap.ec.mobileengage.push

import android.content.Intent
import com.sap.ec.api.push.PushConstants.INTENT_EXTRA_ACTION_KEY
import com.sap.ec.api.push.PushConstants.INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY
import com.sap.ec.api.push.PushConstants.INTENT_EXTRA_PAYLOAD_KEY
import com.sap.ec.core.actions.ActionHandlerApi
import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.PushActionFactoryApi
import com.sap.ec.mobileengage.action.actions.Action
import com.sap.ec.mobileengage.action.models.ActionModel
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.BasicDismissActionModel
import com.sap.ec.mobileengage.action.models.BasicLaunchApplicationActionModel
import com.sap.ec.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.sap.ec.mobileengage.action.models.DismissActionModel
import com.sap.ec.mobileengage.action.models.NotificationOpenedActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.mobileengage.push.model.AndroidPushMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class NotificationIntentProcessor(
    private val json: Json,
    private val actionFactory: PushActionFactoryApi,
    private val actionHandler: ActionHandlerApi,
    private val applicationScope: CoroutineScope,
    private val sdkLogger: Logger
) {
    fun processIntent(intent: Intent?) {
        //TODO: check if SDK setup has been completed

        if (intent != null) {
            applicationScope.launch {
                val triggeredActionModel = getActionModel(intent)
                val triggeredAction = triggeredActionModel?.let {
                    actionFactory.create(it)
                }

                val mandatoryActions = getMandatoryActions(intent, triggeredActionModel)
                actionHandler.handleActions(mandatoryActions, triggeredAction)
            }
        }
    }

    private suspend fun getActionModel(intent: Intent): ActionModel? {
        val action = intent.getStringExtra(INTENT_EXTRA_ACTION_KEY)
        val defaultAction = intent.getStringExtra(INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY)

        return try {
            action?.let { json.decodeFromString<PresentableActionModel>(it) }
                ?: defaultAction?.let { json.decodeFromString<BasicActionModel>(it) }
        } catch (exception: Exception) {
            sdkLogger.error("Action parsing failed", exception, buildJsonObject {
                put("action", action)
                put("defaultAction", defaultAction)
            })
            null
        }
    }

    private suspend fun getMandatoryActions(
        intent: Intent,
        triggeredActionModel: ActionModel?
    ): List<Action<*>> {
        return buildList {
            try {
                val pushMessage =
                    intent.getStringExtra(INTENT_EXTRA_PAYLOAD_KEY)?.let { pushString ->
                        json.decodeFromString<AndroidPushMessage>(pushString)
                    }

                if (triggeredActionModel !is DismissActionModel) {
                    val launchApplicationAction =
                        actionFactory.create(BasicLaunchApplicationActionModel)
                    add(launchApplicationAction)
                    pushMessage?.platformData?.notificationMethod?.collapseId?.let {
                        val dismissAction = actionFactory.create(BasicDismissActionModel(it))
                        add(dismissAction)
                    }
                }

                pushMessage?.let {
                    val reportingAction = when (triggeredActionModel) {
                        is PresentableActionModel -> {
                            BasicPushButtonClickedActionModel(
                                triggeredActionModel.reporting,
                                it.trackingInfo
                            )
                        }

                        is BasicActionModel,
                        null -> {
                            NotificationOpenedActionModel(
                                triggeredActionModel?.reporting,
                                it.trackingInfo
                            )
                        }

                        else -> null
                    }

                    reportingAction?.let { add(actionFactory.create(reportingAction)) }
                }
            } catch (exception: Exception) {
                sdkLogger.error(
                    "Notification intent processor failed",
                    exception
                )
            }
        }
    }
}