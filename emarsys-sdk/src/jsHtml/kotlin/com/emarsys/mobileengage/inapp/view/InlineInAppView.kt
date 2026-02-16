package com.emarsys.mobileengage.inapp.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.embeddedmessaging.ui.toDismissId
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.networking.download.InlineInAppMessageFetcherApi
import io.ktor.http.Url
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.Node
import web.dom.ElementId
import web.dom.document
import web.html.HTMLElement

@Composable
internal actual fun InlineInAppView(message: InAppMessage, onDismiss: () -> Unit) {
    val webViewElement = remember { mutableStateOf<HTMLElement?>(null) }

    LaunchedEffect(message) {
        val inAppViewProvider: InAppViewProviderApi = koin.get()
        val sdkEventDistributor: SdkEventDistributorApi = koin.get()

        val inAppView = inAppViewProvider.provide()
        val holder = inAppView.load(message)

        val webElement = holder.asDynamic().webView as? HTMLElement
        webViewElement.value = webElement

        launch {
            sdkEventDistributor.sdkEventFlow.first { sdkEvent ->
                sdkEvent is SdkEvent.Internal.Sdk.Dismiss && sdkEvent.id == message.dismissId
            }
            webViewElement.value = null
            onDismiss()
        }
    }

    webViewElement.value?.let { webView ->
        Div(attrs = {
            id(message.dismissId.toDismissId())
            style {
                width(100.percent)
                height(100.percent)
            }
            ref { element ->
                element.appendChild(webView.unsafeCast<Node>())
                onDispose { }
            }
        })
    }
}

@Composable
internal fun InlineInAppView(url: Url, trackingInfo: String) {
    val fetcher: InlineInAppMessageFetcherApi = koin.get()
    val message = remember { mutableStateOf<InAppMessage?>(null) }

    LaunchedEffect(url) {

        message.value = fetcher.fetch(url, trackingInfo)
    }

    message.value?.let {
        InlineInAppView(
            message = it,
            onDismiss = {
                removeInlineInApp(it)
            }
        )
    }
}

@Composable
fun InlineInAppView(viewId: String) {
    val message = remember { mutableStateOf<InAppMessage?>(null) }

    LaunchedEffect(viewId) {
        val fetcher: InlineInAppMessageFetcherApi = koin.get()
        message.value = fetcher.fetch(viewId)
    }

    message.value?.let {
        InlineInAppView(it) {}
    }
}

private fun removeInlineInApp(message: InAppMessage) {
    document.getElementById(ElementId(message.dismissId.toDismissId()))?.remove()
}

