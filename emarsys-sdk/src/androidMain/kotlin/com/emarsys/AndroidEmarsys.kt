package com.emarsys

import AndroidConfigApi
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.deeplink.AndroidDeepLinkApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.push.PushApi
import com.emarsys.api.setup.SetupApi
import com.emarsys.core.exceptions.SdkException.SdkAlreadyEnabledException
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.SdkKoinIsolationContext
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.event.SdkEvent
import com.emarsys.init.InitOrganizerApi
import com.emarsys.tracking.TrackingApi
import kotlinx.coroutines.flow.Flow
import org.koin.core.qualifier.named

/**
 * The main entry point for interacting with the Emarsys SDK on Android.
 */
object AndroidEmarsys {

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
     * Provides access to the Android Deep Link API, which allows tracking deep link interactions.
     */
    val deepLink: AndroidDeepLinkApi
        get() = koin.get<AndroidDeepLinkApi>()

    /**
     * Provides access to the In-App API, which allows pausing and resuming the in-app messaging functionality.
     */
    val inApp: InAppApi
        get() = koin.get<InAppApi>()

    /**
     * Provides access to the Config API, which allows retrieving, setting and modifying SDK configuration settings.
     */
    val config: AndroidConfigApi
        get() = koin.get<AndroidConfigApi>()

    /**
     * Initializes the SDK. This method must be called before using any other SDK functionality.
     * On Android it is being called automatically
     */
    suspend fun initialize() {
        SdkKoinIsolationContext.init()
        koin.get<InitOrganizerApi>().init()
    }

    /**
     * Enables tracking with the provided [configuration][config].
     *
     * Example usage:
     * ```kotlin
     *         AndroidEmarsys.enableTracking(
     *             AndroidEmarsysConfig(
     *                 applicationCode = "ABCDE-12345",
     *                 launchActivityClass = MyActivity::class.java,
     *             )
     *         )
     * ```
     *
     * @param config The SDK configuration to use for enabling tracking.
     * @throws SdkAlreadyEnabledException if tracking is already enabled.
     */
    suspend fun enableTracking(config: AndroidEmarsysConfig) {
        koin.get<SetupApi>().enableTracking(config)
    }

    /**
     * Disables tracking.
     */
    suspend fun disableTracking() {
        koin.get<SetupApi>().disableTracking()
    }
}
