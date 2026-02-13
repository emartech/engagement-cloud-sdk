package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.inapp.jsbridge.InAppJsBridgeData
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