package com.sap.ec.mobileengage.inapp.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.di.SdkKoinIsolationContext
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.inapp.InAppMessage
import com.sap.ec.mobileengage.inapp.networking.download.InlineInAppMessageFetcherApi
import com.sap.ec.mobileengage.inapp.presentation.InlineInAppViewRendererApi
import com.sap.ec.mobileengage.inapp.webview.WebViewHolder
import io.ktor.http.Url
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
internal actual fun InlineInAppView(
    message: InAppMessage,
    onDismiss: () -> Unit,
    onLoaded: (() -> Unit)?
) {
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

        onLoaded?.invoke()

        launch {
            sdkEventDistributor.sdkEventFlow.first { sdkEvent ->
                sdkEvent is SdkEvent.Internal.Sdk.Dismiss && sdkEvent.id == message.dismissId
            }
            isVisible.value = false
            onDismiss()
        }
    }

    if (isVisible.value) {
        webViewHolder.value?.let { holder ->
            renderer.Render(holder)
        }
    }
}

@Composable
internal fun InlineInAppView(
    url: Url,
    trackingInfo: String,
    onDismiss: () -> Unit,
    onLoaded: (() -> Unit)? = null
) {
    val sdkContext: SdkContextApi? = koin.getOrNull()
    if (!SdkKoinIsolationContext.isInitialized() || sdkContext?.config?.applicationCode == null) {
        return
    }
    val fetcher: InlineInAppMessageFetcherApi = koin.get()

    val message = rememberSaveable(url, stateSaver = InAppMessageSaver) { mutableStateOf(null) }

    LaunchedEffect(url) {
        message.value = fetcher.fetch(url, trackingInfo)
    }

    message.value?.let {
        InlineInAppView(it, onDismiss, onLoaded)
    }
}

@Composable
fun InlineInAppView(
    viewId: String,
    onLoaded: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
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
        InlineInAppView(
            message = it,
            onDismiss = { onDismiss?.invoke() },
            onLoaded = onLoaded
        )
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