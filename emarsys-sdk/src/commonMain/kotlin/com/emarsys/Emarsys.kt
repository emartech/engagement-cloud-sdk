package com.emarsys

import com.emarsys.api.config.ConfigApi
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.deepLink.DeepLinkApi
import com.emarsys.api.event.EventTrackerApi
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.geofence.GeofenceTrackerApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.push.PushApi
import com.emarsys.core.exceptions.SdkAlreadyEnabledException
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.SdkKoinIsolationContext
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.init.InitOrganizerApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.setup.SetupOrganizerApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.koin.core.qualifier.named
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@OptIn(ExperimentalObjCRefinement::class)
object Emarsys {

    /**
     * Initializes the SDK. This method must be called before using any other SDK functionality.
     * On Android it is being called automatically
     */
    suspend fun initialize() {
        SdkKoinIsolationContext.init()
        koin.get<InitOrganizerApi>().init()
    }

    /**
     * Enables tracking with the provided configuration.
     *
     * @param config The SDK configuration to use for enabling tracking.
     * @throws SdkAlreadyEnabledException if tracking is already enabled.
     */
    suspend fun enableTracking(config: SdkConfig) {
        config.isValid()
        try {
            koin.get<SetupOrganizerApi>().setupWithValidation(config)
        } catch (exception: SdkAlreadyEnabledException) {
            // TODO define error handling and api
        }
    }

    /**
     * Links a contact to the SDK using the specified contact field ID and value.
     *
     * @param contactFieldId The ID of the contact field.
     * @param contactFieldValue The value of the contact field.
     */
    suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        koin.get<ContactApi>().linkContact(contactFieldId, contactFieldValue)
    }

    /**
     * Links an authenticated contact to the SDK using the specified contact field ID and OpenID token.
     * Authenticated contacts are already verified through any OpenID provider like Google or Apple
     *
     * @param contactFieldId The ID of the contact field.
     * @param openIdToken The OpenID token for authentication.
     */
    suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        koin.get<ContactApi>().linkAuthenticatedContact(contactFieldId, openIdToken)
    }

    /**
     * Unlinks the currently linked contact from the SDK. And replaces it with an anonymous contact
     */
    suspend fun unlinkContact() {
        koin.get<ContactApi>().unlinkContact()
    }

    /**
     * Tracks a custom event with the specified name and optional attributes. These custom events can be used to trigger In-App campaigns or any automation configured at Emarsys.
     *
     * @param event The name of the custom event.
     * @param attributes Optional attributes for the event.
     */
    suspend fun trackCustomEvent(event: String, attributes: Map<String, String>? = null) {
        koin.get<EventTrackerApi>().trackEvent(CustomEvent(event, attributes))
    }

    val events: SharedFlow<SdkEvent>
        get() = koin.get<MutableSharedFlow<SdkEvent>>(named(EventFlowTypes.Public))

    val push: PushApi
        get() = koin.get<PushApi>()

    val inApp: InAppApi
        get() = koin.get<InAppApi>()

    val inbox: InboxApi
        get() = koin.get<InboxApi>()

    val config: ConfigApi
        get() = koin.get<ConfigApi>()

    val geofence: GeofenceTrackerApi
        get() = koin.get<GeofenceTrackerApi>()

    val predict: PredictApi
        get() = koin.get<PredictApi>()

    val deepLink: DeepLinkApi
        get() = koin.get<DeepLinkApi>()

}
