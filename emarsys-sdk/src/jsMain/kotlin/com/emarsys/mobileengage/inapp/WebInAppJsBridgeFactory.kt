package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory
import com.emarsys.mobileengage.action.EventActionFactoryApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json

internal class WebInAppJsBridgeFactory(
    private val actionFactory: EventActionFactoryApi,
    private val json: Json,
    private val sdkDispatcher: CoroutineDispatcher
) : Factory<String, WebInAppJsBridge> {
    override fun create(campaignId: String): WebInAppJsBridge {
        return WebInAppJsBridge(actionFactory, json, sdkDispatcher, campaignId)
    }
}