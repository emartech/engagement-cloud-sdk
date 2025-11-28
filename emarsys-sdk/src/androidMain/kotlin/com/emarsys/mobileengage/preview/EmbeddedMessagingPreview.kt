@file:OptIn(ExperimentalTime::class)

package com.emarsys.mobileengage.preview

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
import com.emarsys.mobileengage.embeddedmessaging.ui.category.CategoriesDialogView
import com.emarsys.mobileengage.embeddedmessaging.ui.category.CategorySelectorButton
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemModel
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModel
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageView
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageViewModelApi
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.emarsys.networking.clients.embedded.messaging.model.ListThumbnailImage
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

@Preview(showBackground = true)
@Composable
fun MessageItemViewPreview() {
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
                sdkEventDistributor = previewSdkEventDistributor
            )
        )
    )
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

@Preview(showBackground = true)
@Composable
fun CategorySelectorButtonPreview() {
    CategorySelectorButton(isCategorySelectionActive = false, onClick = {
        println("I'm clicked!")
    })
}

@Preview(showBackground = true)
@Composable
fun CategoriesDialogViewPreview() {
    val messageCategories = listOf(
        MessageCategory(1, "Serums"),
        MessageCategory(2, "Creams"),
        MessageCategory(3, "Boosters"),
        MessageCategory(4, "Promotions"),
        MessageCategory(5, "Beauty tips"),
    )
    CategoriesDialogView(
        messageCategories,
        selectedCategories = setOf(1, 3),
        onApplyClicked = {
            print("I'm applied with categories: $it")
        },
        onDismiss = {
            println("I'm dismissed!")
        }
    )
}

private fun providePreviewMessage() = EmbeddedMessage(
    "testId",
    "Sample Title",
    "This is a sample lead for the embedded message.",
    ListThumbnailImage("https://placebear.com/60/60", null),
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
                                ListThumbnailImage("https://placebear.com/60/60", null),
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
                            sdkEventDistributor = previewSdkEventDistributor
                        )
                    )
                )
            ).asStateFlow()

        override val categories: StateFlow<List<MessageCategory>>
            get() =
                MutableStateFlow(
                    listOf(
                        MessageCategory(1, "Category 1"),
                        MessageCategory(2, "Category 2")
                    )
                ).asStateFlow()


        override val isRefreshing: StateFlow<Boolean>
            get() = MutableStateFlow(false).asStateFlow()

        override val filterUnreadOnly: StateFlow<Boolean>
            get() = MutableStateFlow(false).asStateFlow()
        override val selectedCategoryIds: StateFlow<Set<Int>>
            get() = MutableStateFlow(setOf())

        override fun refreshMessages() {
            Unit
        }

        override fun setFilterUnreadOnly(unreadOnly: Boolean) {
            Unit
        }

        override fun setSelectedCategoryIds(categoryIds: Set<Int>) {
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

    override suspend fun download(urlString: String, fallback: ByteArray?): ByteArray? {
        return Res.readBytes("files/placeholder.png")
    }
}
