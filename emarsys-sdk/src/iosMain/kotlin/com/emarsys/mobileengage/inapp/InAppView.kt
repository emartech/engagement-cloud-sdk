package com.emarsys.mobileengage.inapp

import com.emarsys.core.providers.InstantProvider
import com.emarsys.mobileengage.inapp.jsbridge.ContentReplacerApi
import com.emarsys.mobileengage.inapp.providers.IosWebViewFactoryApi
import com.emarsys.mobileengage.inapp.reporting.InAppLoadingMetric
import com.emarsys.mobileengage.inapp.view.InAppViewApi
import com.emarsys.mobileengage.inapp.webview.WebViewHolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class InAppView(
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewFactory: IosWebViewFactoryApi,
    private val timestampProvider: InstantProvider,
    private val contentReplacer: ContentReplacerApi
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
        val replacedContent = contentReplacer.replace(message.content)
        val webView = withContext(mainDispatcher) {
            webViewFactory.create(message.dismissId, message.trackingInfo).apply {
                loadHTMLString(replacedContent, null)
            }
        }
        return IosWebViewHolder(webView, inAppLoadingMetric())
    }
}
