package com.emarsys

import com.emarsys.api.config.ConfigApi
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.deeplink.AndroidDeepLinkApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.push.PushApi
import com.emarsys.core.exceptions.SdkAlreadyEnabledException
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.event.SdkEvent
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
        get() = Emarsys.contact

    /**
     * Provides access to the Push API, which handles push token management.
     */
    val push: PushApi
        get() = Emarsys.push

    /**
     * Provides access to the Tracking API, which allows tracking custom events.
     */
    val tracking: TrackingApi
        get() = Emarsys.tracking

    /**
     * Provides access to the Android Deep Link API, which allows tracking deep link interactions.
     */
    val deepLink: AndroidDeepLinkApi
        get() = koin.get<AndroidDeepLinkApi>()

    /**
     * Provides access to the In-App API, which allows pausing and resuming the in-app messaging functionality.
     */
    val inApp: InAppApi
        get() = Emarsys.inApp

    /**
     * Provides access to the Inbox API, which allows fetching inbox messages and message tagging functionality.
     */
    val inbox: InboxApi
        get() = Emarsys.inbox

    /**
     * Provides access to the Config API, which allows retrieving, setting and modifying SDK configuration settings.
     */
    val config: ConfigApi
        get() = Emarsys.config

    /**
     * Provides access to the Predict API, which allows tracking user interactions and getting product recommendations.
     */
    val predict: PredictApi
        get() = Emarsys.predict

    /**
     * Initializes the SDK. This method must be called before using any other SDK functionality.
     * On Android it is being called automatically
     */
    suspend fun initialize() {
        Emarsys.initialize()
    }

    /**
     * Enables tracking with the provided [configuration][config].
     *
     * Example usage:
     * ```kotlin
     *         AndroidEmarsys.enableTracking(
     *             AndroidEmarsysConfig(
     *                 applicationCode = "ABCDE-12345",
     *                 merchantId = "merchant_id_example",
     *                 launchActivityClass = MyActivity::class.java,
     *             )
     *         )
     * ```
     *
     * @param config The SDK configuration to use for enabling tracking.
     * @throws SdkAlreadyEnabledException if tracking is already enabled.
     */
    suspend fun enableTracking(config: AndroidEmarsysConfig) {
        Emarsys.enableTracking(config)
    }

    /**
     * Disables tracking.
     */
    suspend fun disableTracking() {
        Emarsys.disableTracking()
    }
}
