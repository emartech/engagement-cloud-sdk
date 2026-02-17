package com.sap.ec.mobileengage.inapp

import com.sap.ec.core.factory.Factory
import com.sap.ec.mobileengage.action.EventActionFactoryApi
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

internal class InAppJsBridgeFactory(
    private val actionFactory: EventActionFactoryApi,
    private val json: Json,
    private val applicationScope: CoroutineScope
) : Factory<InAppJsBridgeData, InAppJsBridge> {
    override fun create(value: InAppJsBridgeData): InAppJsBridge {
        return InAppJsBridge(
            value,
            actionFactory,
            applicationScope,
            json
        )
    }
}