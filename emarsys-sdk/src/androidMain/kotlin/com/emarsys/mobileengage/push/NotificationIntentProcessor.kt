package com.emarsys.mobileengage.push

import android.content.Intent
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_ACTION_KEY
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_PAYLOAD_KEY
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.BasicLaunchApplicationActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class NotificationIntentProcessor(
    private val json: Json,
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val actionHandler: ActionHandlerApi
) {
    fun processIntent(intent: Intent?, lifecycleScope: CoroutineScope) {
        //TODO: check if SDK setup has been completed

        if (intent != null) {
            lifecycleScope.launch {
                val actionModel = getActionModel(intent)
                actionModel?.let {
                    val triggeredAction = actionFactory.create(it)
                    val mandatoryActions = getMandatoryActions(intent, actionModel)
                    actionHandler.handleActions(mandatoryActions, triggeredAction)
                }
            }
        }
    }

    private fun getActionModel(intent: Intent): ActionModel? {
        val action = intent.getStringExtra(INTENT_EXTRA_ACTION_KEY)
        val defaultAction = intent.getStringExtra(INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY)

        return action?.let { json.decodeFromString<PresentableActionModel>(it) }
            ?: defaultAction?.let { json.decodeFromString<BasicActionModel>(it) }
    }

    private suspend fun getMandatoryActions(
        intent: Intent,
        actionModel: ActionModel
    ): List<Action<*>> {
        val result = mutableListOf<Action<*>>()
        val pushMessageString = intent.getStringExtra(INTENT_EXTRA_PAYLOAD_KEY)
        val pushMessage = pushMessageString?.let { json.decodeFromString<AndroidPushMessage>(it) }

        val launchApplicationAction = actionFactory.create(BasicLaunchApplicationActionModel)
        result.add(launchApplicationAction)

        pushMessage?.data?.sid?.let {
            val reportingAction = when (actionModel) {
                is PresentableActionModel -> {
                    BasicPushButtonClickedActionModel(
                        actionModel.id,
                        it
                    )
                }
                is BasicActionModel -> {
                    NotificationOpenedActionModel(it)
                }
                else -> null
            }

            reportingAction?.let {
                result.add(
                    actionFactory.create(reportingAction)
                )
            }
        }

        return result
    }
}