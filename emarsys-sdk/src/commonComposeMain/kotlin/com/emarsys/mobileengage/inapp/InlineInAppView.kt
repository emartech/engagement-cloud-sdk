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
import org.koin.core.qualifier.named

//TODO: when closing the inApp message it won't show again. Should look up how can we handle LaunchedEffect keys differently

@Composable
actual fun InlineInAppView(message: InAppMessage) {
    //TODO: safer koin access in case of uninitialized sdk/koin
    //TODO: koin accesses moved here from launched effect scope
    val inAppViewProvider: InAppViewProviderApi = koin.get()
    val sdkEventDistributor: SdkEventDistributorApi = koin.get()
    val renderer: InlineInAppViewRendererApi = koin.get()

    //TODO: check configuration change
    val webViewHolder = remember { mutableStateOf<WebViewHolder?>(null) }

    LaunchedEffect(message) {
        val inAppView = inAppViewProvider.provide()
        val holder = inAppView.load(message)
        webViewHolder.value = holder

        launch {
            sdkEventDistributor.sdkEventFlow.first { sdkEvent ->
                sdkEvent is SdkEvent.Internal.Sdk.Dismiss && sdkEvent.id == message.dismissId
            }

            webViewHolder.value = null
        }
    }

    webViewHolder.value?.let { holder ->
        renderer.Render(holder)
    }
}

@Composable
internal fun InlineInAppView(url: Url) {
    //TODO: safer koin access in case of uninitialized sdk/koin
    //TODO: move fetching into the fetcher with a dedicated method, get rid of emarsysClient here
    val emarsysClient: NetworkClientApi = koin.get(named(NetworkClientTypes.Emarsys))
    //TODO: check possibilities to survive config changes
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
    //TODO: safer koin access in case of uninitialized sdk/koin
    //TODO: move koin access from launched effect scope
    val fetcher: InlineInAppMessageFetcherApi = koin.get()
    //TODO: check possibilities to survive config changes
    val message = remember { mutableStateOf<InAppMessage?>(null) }

    LaunchedEffect(viewId) {
        message.value = fetcher.fetch(viewId)
    }

    message.value?.let {
        InlineInAppView(it)
    }
}

