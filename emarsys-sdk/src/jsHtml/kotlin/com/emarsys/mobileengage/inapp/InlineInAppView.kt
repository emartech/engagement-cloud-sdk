package com.emarsys.mobileengage.inapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.di.NetworkClientTypes
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.event.SdkEvent
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.koin.core.qualifier.named
import org.w3c.dom.Node
import web.html.HTMLElement

@Composable
actual fun InlineInAppView(message: InAppMessage) {
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
        }
    }

    webViewElement.value?.let { webView ->
        Div(attrs = {
            id(message.dismissId)
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
internal fun InlineInAppView(url: Url) {
    val emarsysClient: NetworkClientApi = koin.get(named(NetworkClientTypes.Emarsys))
    val message = remember { mutableStateOf<InAppMessage?>(null) }

    LaunchedEffect(url) {
        val response = emarsysClient.send(UrlRequest(url, HttpMethod.Get))

        response.getOrNull()?.let {
            message.value = InAppMessage(
                type = InAppType.INLINE,
                trackingInfo = "inlineInAppTrackingInfo",
                content = it.bodyAsText
            )
        }
    }

    message.value?.let {
        InlineInAppView(it)
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
        InlineInAppView(it)
    }
}

