@file:OptIn(ExperimentalTime::class)

package com.emarsys.mobileengage.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.actions.OpenExternalUrlAction
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants
import com.emarsys.mobileengage.embeddedmessaging.ui.category.CategoriesDialogView
import com.emarsys.mobileengage.embeddedmessaging.ui.category.CategorySelectorButton
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemModel
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModel
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.InternalListPageView
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageViewModelApi
import com.emarsys.networking.clients.embedded.messaging.model.Category
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.emarsys.networking.clients.embedded.messaging.model.ListThumbnailImage
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.util.decodeBase64Bytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.JsonObject
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
                sdkEventDistributor = previewSdkEventDistributor,
                actionFactory = PreviewActionFactory(),
                logger = PreviewLogger()
            )
        ),
        onClick = {
            println("Message item clicked!")
        }
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
    InternalListPageView(
        showFilters = true,
        viewModel = providePreviewMessageViewModel(previewSdkEventDistributor)
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
        selectedCategoriesOnDialogOpen = setOf(1, 3),
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
    listOf(Category(1, "1"), Category(2, "2")),
    Clock.System.now().minus(3.hours).toEpochMilliseconds(),
    Clock.System.now().plus(4.days).toEpochMilliseconds(),
    mapOf("key1" to "value1", "key2" to "value2"),
    "tracking_info_example"
)

@OptIn(ExperimentalTime::class)
private fun providePreviewMessageViewModel(previewSdkEventDistributor: SdkEventDistributorApi) =
    object : ListPageViewModelApi {
        override val messagePagingDataFlowFiltered: Flow<PagingData<MessageItemViewModelApi>>
            get() = flowOf(
                PagingData.from(
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
                                    listOf(Category(1, "1"), Category(2, "2")),
                                    Clock.System.now().minus(3.hours).toEpochMilliseconds(),
                                    Clock.System.now().plus(4.days).toEpochMilliseconds(),
                                    mapOf("key1" to "value1", "key2" to "value2"),
                                    "tracking_info_example"
                                ),
                                downloaderApi = PreviewDownLoader(),
                                sdkEventDistributor = previewSdkEventDistributor,
                                actionFactory = PreviewActionFactory(),
                                logger = PreviewLogger()
                            )
                        ),
                        MessageItemViewModel(
                            MessageItemModel(
                                EmbeddedMessage(
                                    "testId2",
                                    "Sample Title 2",
                                    "This is a sample lead for the embedded message.",
                                    ListThumbnailImage("https://placebear.com/60/60", null),
                                    BasicOpenExternalUrlActionModel(
                                        reporting = "Default Action", url = "https://example.com"
                                    ),
                                    emptyList(),
                                    listOf("promo", "new"),
                                    listOf(Category(1, "1"), Category(2, "2")),
                                    Clock.System.now().minus(3.hours).toEpochMilliseconds(),
                                    Clock.System.now().plus(4.days).toEpochMilliseconds(),
                                    mapOf("key1" to "value1", "key2" to "value2"),
                                    "tracking_info_example"
                                ),
                                downloaderApi = PreviewDownLoader(),
                                sdkEventDistributor = previewSdkEventDistributor,
                                actionFactory = PreviewActionFactory(),
                                logger = PreviewLogger()
                            )
                        )
                    )
                )
            )

        override val categories: StateFlow<List<MessageCategory>>
            get() =
                MutableStateFlow(
                    listOf(
                        MessageCategory(1, "Category 1"),
                        MessageCategory(2, "Category 2")
                    )
                ).asStateFlow()

        override val filterUnopenedOnly: StateFlow<Boolean>
            get() = MutableStateFlow(false).asStateFlow()

        override val selectedCategoryIds: StateFlow<Set<Int>>
            get() = MutableStateFlow<Set<Int>>(setOf()).asStateFlow()

        override val hasFiltersApplied: StateFlow<Boolean>
            get() = MutableStateFlow(false).asStateFlow()
        override val hasConnection: StateFlow<Boolean>
            get() = MutableStateFlow(false).asStateFlow()

        override val selectedMessage: StateFlow<MessageItemViewModelApi?>
            get() = MutableStateFlow(null).asStateFlow()

        override val showCategorySelector: StateFlow<Boolean>
            get() = MutableStateFlow(false).asStateFlow()

        override val triggerRefreshFromJs: () -> Unit = { Unit }

        override val shouldHideFilterRowForDetailedView: StateFlow<Boolean>
            get() = MutableStateFlow(false).asStateFlow()

        override fun setFilterUnopenedOnly(unreadOnly: Boolean) {
            Unit
        }

        override fun setSelectedCategoryIds(categoryIds: Set<Int>) {
            Unit
        }

        override suspend fun selectMessage(
            messageViewModel: MessageItemViewModelApi,
            navigationCallback: suspend () -> Unit
        ) {
            Unit
        }

        override suspend fun deleteMessage(messageViewModel: MessageItemViewModelApi): Result<Unit> {
            return Result.success(Unit)
        }

        override fun clearMessageSelection() {
            Unit
        }

        override fun openCategorySelector() {
            Unit
        }

        override fun closeCategorySelector() {
            Unit
        }

        override fun applyCategorySelection(categoryIds: Set<Int>) {
            Unit
        }

        override fun refreshMessagesWithThrottling(shouldCallRefresh: () -> Unit) {
            Unit
        }

        override fun hideFilterRowForDetailedView(shouldHideFilterRow: Boolean) {
            Unit
        }
    }


internal class PreviewActionFactory() : ActionFactoryApi<ActionModel> {
    override suspend fun create(action: ActionModel): Action<*> {
        return OpenExternalUrlAction(
            action = BasicOpenExternalUrlActionModel(url = "https://www.example.com"),
            externalUrlOpener = object : ExternalUrlOpenerApi {
                override suspend fun open(url: String) {
                    println("Opening URL: $url")
                }
            })
    }
}


internal class PreviewDownLoader : DownloaderApi {
    private val client = HttpClient {

        install(HttpRequestRetry)
    }

    override suspend fun download(urlString: String, fallback: ByteArray?): ByteArray? {
        return EmbeddedMessagingConstants.Image.BASE64_PLACEHOLDER_IMAGE.decodeBase64Bytes()
    }
}

internal class PreviewLogger : Logger {
    override suspend fun info(
        logEntry: LogEntry,
        isRemoteLog: Boolean
    ) {
        Unit
    }

    override suspend fun info(message: String, isRemoteLog: Boolean) {
        Unit
    }

    override suspend fun info(
        message: String,
        throwable: Throwable,
        isRemoteLog: Boolean
    ) {
        Unit
    }

    override suspend fun info(
        message: String,
        data: JsonObject,
        isRemoteLog: Boolean
    ) {
        Unit
    }

    override suspend fun trace(message: String, isRemoteLog: Boolean) {
        Unit
    }

    override suspend fun trace(
        message: String,
        data: JsonObject,
        isRemoteLog: Boolean
    ) {
        Unit
    }

    override suspend fun debug(
        logEntry: LogEntry,
        isRemoteLog: Boolean
    ) {
        Unit
    }

    override suspend fun debug(message: String, isRemoteLog: Boolean) {
        Unit
    }

    override suspend fun debug(
        message: String,
        data: JsonObject,
        isRemoteLog: Boolean
    ) {
        Unit
    }

    override suspend fun debug(
        message: String,
        throwable: Throwable,
        isRemoteLog: Boolean
    ) {
        Unit
    }

    override suspend fun error(
        logEntry: LogEntry,
        isRemoteLog: Boolean
    ) {
        Unit
    }

    override suspend fun error(message: String, isRemoteLog: Boolean) {
        Unit
    }

    override suspend fun error(
        message: String,
        data: JsonObject,
        isRemoteLog: Boolean
    ) {
        Unit
    }

    override suspend fun error(
        message: String,
        throwable: Throwable,
        isRemoteLog: Boolean
    ) {
        Unit
    }

    override suspend fun error(
        message: String,
        throwable: Throwable,
        data: JsonObject,
        isRemoteLog: Boolean
    ) {
        Unit
    }

    override suspend fun metric(
        message: String,
        data: JsonObject
    ) {
        Unit
    }

}
