package com.emarsys.mobileengage.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.emarsys_sdk.generated.resources.Res
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.embeddedmessaging.provider.FallbackImageProvider
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemModel
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModel
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageModel
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageView
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageViewModel
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageViewModelApi
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.emarsys.util.JsonUtil
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

@Preview(showBackground = true)
@Composable
fun MessageItemViewPreview() {
    MaterialTheme {
        val previewSdkEventDistributor = object : SdkEventDistributorApi {
            override val sdkEventFlow =
                MutableSharedFlow<SdkEvent>()
            override val onlineSdkEvents =
                emptyFlow<OnlineSdkEvent>()
            override val logEvents =
                emptyFlow<SdkEvent.Internal.LogEvent>()

            override suspend fun registerEvent(sdkEvent: SdkEvent) =
                throw NotImplementedError("Preview only")
        }
        MessageItemView(
            MessageItemViewModel(
                MessageItemModel(
                    message = providePreviewMessage(),
                    downloaderApi = PreviewDownLoader(),
                    fallbackImageProvider = FallbackImageProvider(),
                    sdkEventDistributor = previewSdkEventDistributor
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListPageViewPreview() {
    val coroutineScope = remember {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.cancel()
        }
    }

    MaterialTheme {
        val previewSdkEventDistributor = object : SdkEventDistributorApi {
            override val sdkEventFlow =
                MutableSharedFlow<SdkEvent>()
            override val onlineSdkEvents =
                emptyFlow<OnlineSdkEvent>()
            override val logEvents =
                emptyFlow<SdkEvent.Internal.LogEvent>()

            override suspend fun registerEvent(sdkEvent: SdkEvent) =
                throw NotImplementedError("Preview only")
        }
        ListPageView(
            providePreviewMessageViewModel(previewSdkEventDistributor)
        )
    }
}

private fun providePreviewMessage() = EmbeddedMessage(
    "testId",
    "Sample Title",
    "This is a sample lead for the embedded message.",
    "https://placebear.com/60/60",
    BasicOpenExternalUrlActionModel(
        reporting = "Default Action", url = "https://example.com"
    ),
    emptyList(),
    listOf("promo", "new"),
    listOf(1, 2),
    Clock.System.now().minus(3.hours).toEpochMilliseconds(),
    Clock.System.now().plus(4.days).toEpochMilliseconds(),
    mapOf("key1" to "value1", "key2" to "value2"),
    "tracking_info_example"
)

@OptIn(ExperimentalTime::class)
private fun providePreviewMessageViewModel(previewSdkEventDistributor: SdkEventDistributorApi) =
    object : ListPageViewModelApi {
        override val messages: StateFlow<List<MessageItemViewModel>>
            get() = MutableStateFlow(
                listOf(
                    MessageItemViewModel(
                        MessageItemModel(
                            EmbeddedMessage(
                                "testId",
                                "Sample Title",
                                "This is a sample lead for the embedded message.",
                                "https://placebear.com/60/60",
                                BasicOpenExternalUrlActionModel(
                                    reporting = "Default Action", url = "https://example.com"
                                ),
                                emptyList(),
                                listOf("promo", "new"),
                                listOf(1, 2),
                                Clock.System.now().minus(3.hours).toEpochMilliseconds(),
                                Clock.System.now().plus(4.days).toEpochMilliseconds(),
                                mapOf("key1" to "value1", "key2" to "value2"),
                                "tracking_info_example"
                            ),
                            downloaderApi = PreviewDownLoader(),
                            fallbackImageProvider = FallbackImageProvider(),
                            sdkEventDistributor = previewSdkEventDistributor
                        )
                    )
                )
            ).asStateFlow()

        override fun refreshMessages() {
            Unit
        }
    }


class PreviewDownLoader : DownloaderApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(JsonUtil.json)
        }
        install(HttpRequestRetry)
    }

    override suspend fun download(urlString: String): ByteArray? {
        return Res.readBytes("files/placeholder.png")
    }
}
