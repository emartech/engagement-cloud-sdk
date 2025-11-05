package com.emarsys.mobileengage.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.emarsys.core.util.DownloaderApi
import com.emarsys.emarsys_sdk.generated.resources.Res
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModel
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.emarsys.util.JsonUtil
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Preview(showBackground = true)
@Composable
fun MessageItemViewPreview() {
    MaterialTheme {
        MessageItemView(
            providePreviewMessage(),
            MessageItemViewModel(PreviewDownLoader())
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
