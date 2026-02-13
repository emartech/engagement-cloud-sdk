package com.emarsys.mobileengage.inapp.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.InstantProvider
import com.emarsys.mobileengage.inapp.AndroidWebViewHolder
import com.emarsys.mobileengage.inapp.InAppJsBridge
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.jsbridge.InAppJsBridgeData
import com.emarsys.mobileengage.inapp.provider.WebViewProvider
import com.emarsys.mobileengage.inapp.reporting.InAppLoadingMetric
import com.emarsys.mobileengage.inapp.webview.WebViewHolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalTime::class)
internal class InAppView @JvmOverloads constructor(
    context: Context,
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: WebViewProvider,
    private val jsBridgeFactory: Factory<InAppJsBridgeData, InAppJsBridge>,
    private val timestampProvider: InstantProvider,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), InAppViewApi {

    companion object {
        const val JS_BRIDGE_NAME = "Android"
    }

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

        return withContext(mainDispatcher) {
            val webView = webViewProvider.provide()
            webView.loadDataWithBaseURL(null, message.content, "text/html", "UTF-8", null)

            addView(webView)
            webView.addJavascriptInterface(
                jsBridgeFactory.create(InAppJsBridgeData(message.dismissId, message.trackingInfo)),
                JS_BRIDGE_NAME
            )

            AndroidWebViewHolder(webView, inAppLoadingMetric())
        }
    }
}