package com.sap.ec.mobileengage.inapp

import com.sap.ec.core.factory.Factory
import com.sap.ec.mobileengage.action.EventActionFactoryApi
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json

internal class WebInAppJsBridgeFactory(
    private val actionFactory: EventActionFactoryApi,
    private val json: Json,
    private val sdkDispatcher: CoroutineDispatcher
) : Factory<InAppJsBridgeData, WebInAppJsBridge> {
    override fun create(value: InAppJsBridgeData): WebInAppJsBridge {
        return WebInAppJsBridge(actionFactory, value, sdkDispatcher, json)
    }
}