package com.emarsys.mobileengage.inapp

import com.emarsys.core.providers.Provider
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

class InAppJsBridgeProvider(
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val json: Json,
    private val sdkScope: CoroutineScope
) :
    Provider<InAppJsBridge> {
    override fun provide(): InAppJsBridge {
        return InAppJsBridge(actionFactory, json, sdkScope)
    }
}
