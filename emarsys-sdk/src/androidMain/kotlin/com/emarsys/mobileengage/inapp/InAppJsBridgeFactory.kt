package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory
import com.emarsys.mobileengage.action.EventActionFactoryApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json

internal class InAppJsBridgeFactory(
    private val actionFactory: EventActionFactoryApi,
    private val json: Json,
    private val sdkDispatcher: CoroutineDispatcher
) :
    Factory<String, InAppJsBridge> {
    override fun create(value: String): InAppJsBridge {
        return InAppJsBridge(value, actionFactory, sdkDispatcher, json)
    }
}