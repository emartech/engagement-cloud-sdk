package com.sap.ec

import com.sap.ec.api.config.AndroidConfigApi
import com.sap.ec.api.contact.ContactApi
import com.sap.ec.api.deeplink.AndroidDeepLinkApi
import com.sap.ec.api.embeddedmessaging.EmbeddedMessagingApi
import com.sap.ec.api.inapp.InAppApi
import com.sap.ec.api.push.PushApi
import com.sap.ec.api.setup.AndroidSetupApi
import com.sap.ec.di.EventFlowTypes
import com.sap.ec.di.SdkKoinIsolationContext
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.event.SdkEvent
import com.sap.ec.tracking.TrackingApi
import kotlinx.coroutines.flow.Flow
import org.koin.core.qualifier.named

/**
 * The main entry point for interacting with the Engagement Cloud SDK on Android.
 */
object AndroidEngagementCloud {

    /**
     * Publishes a flow of SDK events that can be observed externally.
     * The following event types are available:
     * - [SdkEvent.External.Api.AppEvent] - represents events defined by
     * the SAP Engagement Cloud platform user.
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
     * Provides access to the Setup API, which allows enabling and disabling the tracking in the SDK.
     */
    val setup: AndroidSetupApi
        get() = koin.get<AndroidSetupApi>()

    /**
     * Provides access to the Embedded Messaging API, which allows access to EmbeddedMessaging state.
     */
    val embeddedMessaging: EmbeddedMessagingApi
        get() = koin.get<EmbeddedMessagingApi>()

    /**
     * Initializes the SDK. This method must be called before using any other SDK functionality.
     * On Android it is being called automatically
     */
    suspend fun initialize() {
        SdkKoinIsolationContext.init()
    }
}
