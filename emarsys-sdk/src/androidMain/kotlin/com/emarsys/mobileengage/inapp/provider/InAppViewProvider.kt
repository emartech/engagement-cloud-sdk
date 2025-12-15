package com.emarsys.mobileengage.inapp.provider

import android.content.Context
import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.InstantProvider
import com.emarsys.mobileengage.inapp.InAppJsBridge
import com.emarsys.mobileengage.inapp.InAppJsBridgeData
import com.emarsys.mobileengage.inapp.InAppViewApi
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.view.InAppView
import kotlinx.coroutines.CoroutineDispatcher

internal class InAppViewProvider(
    private val applicationContext: Context,
    private val jsBridgeFactory: Factory<InAppJsBridgeData, InAppJsBridge>,
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: WebViewProvider,
    private val timestampProvider: InstantProvider
) : InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        val inAppView = InAppView(
            applicationContext,
            mainDispatcher,
            webViewProvider,
            jsBridgeFactory,
            timestampProvider
        )
        return inAppView
    }
}
