package com.emarsys.mobileengage.inapp.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.di.SdkKoinIsolationContext
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.networking.download.InlineInAppMessageFetcherApi
import com.emarsys.mobileengage.inapp.presentation.InlineInAppViewRendererApi
import com.emarsys.mobileengage.inapp.webview.WebViewHolder
import io.ktor.http.Url
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
actual fun InlineInAppView(message: InAppMessage) {
    val sdkContext: SdkContextApi? = koin.getOrNull()
    if (!SdkKoinIsolationContext.isInitialized() || sdkContext?.config?.applicationCode == null) {
        return
    }
    val inAppViewProvider: InAppViewProviderApi = koin.get()
    val sdkEventDistributor: SdkEventDistributorApi = koin.get()
    val renderer: InlineInAppViewRendererApi = koin.get()

    val webViewHolder = remember { mutableStateOf<WebViewHolder?>(null) }
    val isVisible = rememberSaveable(message.dismissId) { mutableStateOf(true) }

    LaunchedEffect(message) {
        val inAppView = inAppViewProvider.provide()
        val holder = inAppView.load(message)
        webViewHolder.value = holder

        launch {
            sdkEventDistributor.sdkEventFlow.first { sdkEvent ->
                sdkEvent is SdkEvent.Internal.Sdk.Dismiss && sdkEvent.id == message.dismissId
            }
            isVisible.value = false
        }
    }

    if (isVisible.value) {
        webViewHolder.value?.let { holder ->
            renderer.Render(holder)
        }
    }
}

@Composable
internal fun InlineInAppView(url: Url) {
    val sdkContext: SdkContextApi? = koin.getOrNull()
    if (!SdkKoinIsolationContext.isInitialized() || sdkContext?.config?.applicationCode == null) {
        return
    }
    val fetcher: InlineInAppMessageFetcherApi = koin.get()
    
    val message = rememberSaveable(url, stateSaver = InAppMessageSaver) { mutableStateOf(null) }

    LaunchedEffect(url) {
        message.value = fetcher.fetch(url)
    }

    message.value?.let {
        InlineInAppView(it)
    }
}

@Composable
fun InlineInAppView(viewId: String) {
    val sdkContext: SdkContextApi? = koin.getOrNull()
    if (!SdkKoinIsolationContext.isInitialized() || sdkContext?.config?.applicationCode == null) {
        return
    }

    val fetcher: InlineInAppMessageFetcherApi = koin.get()
    
    val message = rememberSaveable(viewId, stateSaver = InAppMessageSaver) { mutableStateOf(null) }

    LaunchedEffect(viewId) {
        message.value = fetcher.fetch(viewId)
    }

    message.value?.let {
        InlineInAppView(it)
    }
}

private val InAppMessageSaver = Saver<InAppMessage?, String>(
    save = { state ->
        state?.let { Json.encodeToString(it) } ?: ""
    },
    restore = { jsonString ->
        if (jsonString.isNotEmpty()) Json.decodeFromString(jsonString) else null
    }
)