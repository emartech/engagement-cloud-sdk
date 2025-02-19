package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

class InAppJsBridgeFactory(
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val json: Json,
    private val sdkDispatcher: CoroutineDispatcher
) : Factory<String, InAppJsBridge> {
    override fun create(campaignId: String): InAppJsBridge {
        return InAppJsBridge(actionFactory, json, CoroutineScope(sdkDispatcher), campaignId)
    }
}