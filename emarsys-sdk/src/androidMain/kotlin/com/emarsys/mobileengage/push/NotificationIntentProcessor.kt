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
import com.emarsys.mobileengage.action.models.BasicDismissActionModel
import com.emarsys.mobileengage.action.models.BasicLaunchApplicationActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.DismissActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class NotificationIntentProcessor(
    private val json: Json,
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val actionHandler: ActionHandlerApi,
    private val pushMessageFactory: AndroidPushMessageFactory
) {
    fun processIntent(intent: Intent?, lifecycleScope: CoroutineScope) {
        //TODO: check if SDK setup has been completed

        if (intent != null) {
            lifecycleScope.launch {
                val triggeredActionModel = getActionModel(intent)
                triggeredActionModel?.let {
                    val triggeredAction = actionFactory.create(it)
                    val mandatoryActions = getMandatoryActions(intent, triggeredActionModel)
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
        triggeredActionModel: ActionModel
    ): List<Action<*>> {
        val result = mutableListOf<Action<*>>()
        val pushMessage = intent.getStringExtra(INTENT_EXTRA_PAYLOAD_KEY)?.let { pushString ->
            json.decodeFromString<JsonObject>(pushString).let { pushJson ->
                pushMessageFactory.create(pushJson)
            }
        }

        if (triggeredActionModel !is DismissActionModel) {
            val launchApplicationAction = actionFactory.create(BasicLaunchApplicationActionModel)
            result.add(launchApplicationAction)
            pushMessage?.platformData?.notificationMethod?.collapseId?.let {
                val dismissAction =
                    actionFactory.create(BasicDismissActionModel(it))
                result.add(dismissAction)
            }
        }

        pushMessage?.sid?.let {
            val reportingAction = when (triggeredActionModel) {
                is PresentableActionModel -> {
                    BasicPushButtonClickedActionModel(
                        triggeredActionModel.id,
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