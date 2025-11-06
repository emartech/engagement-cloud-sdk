package com.emarsys

import androidx.compose.runtime.Composable
import com.emarsys.api.config.ConfigApi
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.deeplink.DeepLinkApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.push.PushApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.SdkKoinIsolationContext
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.event.SdkEvent
import com.emarsys.init.InitOrganizerApi
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModel
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.emarsys.tracking.TrackingApi
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import org.koin.core.qualifier.named
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@HiddenFromObjC
@OptIn(ExperimentalObjCRefinement::class)
object Emarsys {

    internal fun initDI() {
        SdkKoinIsolationContext.init()
    }

    internal suspend fun runInitOrganizer() {
        return koin.get<InitOrganizerApi>().init()
    }

    suspend fun initialize(): Result<Unit> {
        return runCatching {
            initDI()
            runInitOrganizer()
        }
    }

    /**
     * Publishes a flow of SDK events that can be observed externally.
     * The following event types are available:
     * - [AppEvent][SdkEvent.External.Api.AppEvent] - represents events defined by
     * the SAP Emarsys platform user.
     * - [BadgeCountEvent][SdkEvent.External.Api.BadgeCountEvent] - represents changes in the badge count.
     */
    val events: Flow<SdkEvent>
        get() = koin.get<Flow<SdkEvent.External.Api>>(named(EventFlowTypes.Public))

    /**
     * Provides access to the Contact API, which allows managing the contact using the SDK.
     */
    val contact: ContactApi
        get() = koin.get<ContactApi>()

    /**
     * Provides access to the Push API, which handles push token management.
     */
    val push: PushApi
        get() = koin.get<PushApi>()

    /**
     * Provides access to the Event Tracking API, which allows tracking custom events.
     */
    val event: TrackingApi
        get() = koin.get<TrackingApi>()

    /**
     * Provides access to the In-App API, which allows pausing and resuming the in-app messaging functionality.
     */
    val inApp: InAppApi
        get() = koin.get<InAppApi>()

    /**
     * Provides access to the Config API, which allows retrieving, setting and modifying SDK configuration settings.
     */
    val config: ConfigApi
        get() = koin.get<ConfigApi>()

    /**
     * Provides access to the Deep Link API, which allows tracking deep link interactions.
     */
    val deepLink: DeepLinkApi
        get() = koin.get<DeepLinkApi>()

    @Composable
    fun TestMessageItemView() {
        val downloader = koin.get<DownloaderApi>()
        val viewModel = MessageItemViewModel(downloader)
        MessageItemView(
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
            viewModel
        )
    }
}
