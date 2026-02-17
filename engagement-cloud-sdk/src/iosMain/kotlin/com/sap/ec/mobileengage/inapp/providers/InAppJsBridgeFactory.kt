package com.sap.ec.mobileengage.inapp.providers

import com.sap.ec.core.factory.Factory
import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.ActionFactoryApi
import com.sap.ec.mobileengage.action.models.ActionModel
import com.sap.ec.mobileengage.inapp.InAppJsBridge
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
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
