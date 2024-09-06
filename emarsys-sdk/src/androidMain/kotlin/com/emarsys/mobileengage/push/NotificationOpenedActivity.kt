package com.emarsys.mobileengage.push

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_ACTION_KEY
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY
import com.emarsys.di.DependencyContainerPrivateApi
import com.emarsys.di.DependencyInjection
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import kotlinx.coroutines.launch

class NotificationOpenedActivity : AppCompatActivity() {
    private val container = DependencyInjection.container as DependencyContainerPrivateApi
    private val json = container.json
    private val actionFactory = container.pushActionFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processIntent()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent()
    }

    private fun processIntent() {
        //TODO: check if SDK setup has been completed

        if (intent != null) {
            lifecycleScope.launch {
                val actionModel = getActionModel(intent)
                actionModel?.let { actionFactory.create(it).invoke() }
                finish()
            }
        } else {
            finish()
        }
    }

    private fun getActionModel(intent: Intent): ActionModel? {
        val action = intent.getStringExtra(INTENT_EXTRA_ACTION_KEY)
        val defaultAction = intent.getStringExtra(INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY)

        return action?.let { json.decodeFromString<PresentableActionModel>(it) }
            ?: defaultAction?.let { json.decodeFromString<BasicActionModel>(it) }
    }
}