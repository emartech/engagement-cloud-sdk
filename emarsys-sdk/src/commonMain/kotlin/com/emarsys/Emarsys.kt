package com.emarsys

import com.emarsys.api.config.ConfigApi
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.deeplink.DeepLinkApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.push.PushApi
import com.emarsys.api.setup.SetupApi
import com.emarsys.config.SdkConfig
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.SdkKoinIsolationContext
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.event.SdkEvent
import com.emarsys.init.InitOrganizerApi
import com.emarsys.tracking.TrackingApi
import kotlinx.coroutines.flow.Flow
import org.koin.core.qualifier.named
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

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
     * Enables tracking with the provided [configuration][config].
     *
     * Example usage on Android:
     * ```kotlin
     *         Emarsys.enableTracking(
     *             AndroidEmarsysConfig(
     *                 applicationCode = "ABCDE-12345",
     *                 launchActivityClass = MyActivity::class.java
     *             )
     *         )
     * ```
     *
     * @param config The SDK configuration to use for enabling tracking.
     * @returns a [Result] indicating the result of the operation,
     * containing SdkAlreadyEnabledException if tracking is already enabled.
     */
    suspend fun enableTracking(config: SdkConfig): Result<Unit> {
        return koin.get<SetupApi>().enableTracking(config)
    }

    /**
     * Disables tracking.
     * @returns a [Result] indicating the result of the operation,
     * containing SdkAlreadyDisabledException if tracking is already disabled.
     */
    suspend fun disableTracking(): Result<Unit> {
        return koin.get<SetupApi>().disableTracking()
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
     * Provides access to the Tracking API, which allows tracking custom events.
     */
    val tracking: TrackingApi
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

}
