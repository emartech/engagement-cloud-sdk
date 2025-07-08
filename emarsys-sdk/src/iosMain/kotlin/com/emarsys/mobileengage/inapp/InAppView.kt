package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.SuspendFactory
import com.emarsys.core.providers.InstantProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import platform.WebKit.WKWebView
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class InAppView(
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: SuspendFactory<String, WKWebView>,
    private val timestampProvider: InstantProvider
    ) : InAppViewApi {
    private lateinit var mInAppMessage: InAppMessage
    private var loadingStarted: Long? = null
    override val inAppMessage: InAppMessage
        get() = mInAppMessage

    private fun inAppLoadingMetric(): InAppLoadingMetric {
        return InAppLoadingMetric(
            loadingStarted = loadingStarted ?: 0,
            loadingEnded = timestampProvider.provide().toEpochMilliseconds()
        )
    }

    override suspend fun load(message: InAppMessage): WebViewHolder {
        loadingStarted = timestampProvider.provide().toEpochMilliseconds()

        mInAppMessage = message
        return IosWebViewHolder(withContext(mainDispatcher) {
            val webView = webViewProvider.create(message.trackingInfo)
            webView.loadHTMLString(message.content, null)
            webView
        }, inAppLoadingMetric())
    }
}
