package com.emarsys.mobileengage.inapp

import android.content.Context
import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import kotlinx.coroutines.CoroutineDispatcher

internal class InAppViewProvider(
    private val applicationContext: Context,
    private val jsBridgeFactory: Factory<InAppJsBridgeData, InAppJsBridge>,
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: WebViewProvider,
    private val timestampProvider: InstantProvider,
    private val uuidProvider: UuidProviderApi,
) : InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        val inAppView = InAppView(
            applicationContext,
            mainDispatcher,
            webViewProvider,
            jsBridgeFactory,
            timestampProvider,
            uuidProvider
        )
        return inAppView
    }
}
