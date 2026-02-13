package com.emarsys.mobileengage.inapp.providers

import com.emarsys.core.factory.Factory
import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.inapp.InAppJsBridge
import com.emarsys.mobileengage.inapp.jsbridge.InAppJsBridgeData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json

class InAppJsBridgeFactory(
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val json: Json,
    private val mainDispatcher: CoroutineDispatcher,
    private val sdkDispatcher: CoroutineDispatcher,
    private val sdkLogger: Logger
) : Factory<InAppJsBridgeData, InAppJsBridge> {
    override fun create(value: InAppJsBridgeData): InAppJsBridge {
        return InAppJsBridge(
            actionFactory,
            value,
            mainDispatcher,
            sdkDispatcher,
            sdkLogger,
            json
        )
    }
}
