package com.emarsys

import com.emarsys.api.config.ConfigApi
import com.emarsys.api.deepLink.DeepLinkApi
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.geofence.GeofenceTrackerApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.push.PushApi
import com.emarsys.core.exceptions.SdkAlreadyEnabledException
import com.emarsys.di.DependencyInjection

import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@OptIn(ExperimentalObjCRefinement::class)
object Emarsys {

    suspend fun initialize() {
        DependencyInjection.container.setup()
    }

    suspend fun enableTracking(config: SdkConfig) {
        config.isValid()
        try {
            DependencyInjection.container.setupOrganizerApi.setupWithValidation(config)
        } catch (exception: SdkAlreadyEnabledException) {
            // TODO define error handling and api
        }
    }

    suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        DependencyInjection.container.contactApi.linkContact(contactFieldId, contactFieldValue)
    }

    suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        DependencyInjection.container.contactApi.linkAuthenticatedContact(
            contactFieldId,
            openIdToken
        )
    }

    suspend fun unlinkContact() {
        DependencyInjection.container.contactApi.unlinkContact()
    }

    suspend fun trackCustomEvent(event: String, attributes: Map<String, String>? = null) {
        DependencyInjection.container.eventTrackerApi.trackEvent(CustomEvent(event, attributes))
    }

    val events: SharedFlow<SdkEvent>
        get() = DependencyInjection.container.events

    val push: PushApi
        get() = DependencyInjection.container.pushApi

    val inApp: InAppApi
        get() = DependencyInjection.container.inAppApi

    val inbox: InboxApi
        get() = DependencyInjection.container.inboxApi

    val config: ConfigApi
        get() = DependencyInjection.container.configApi

    val geofence: GeofenceTrackerApi
        get() = DependencyInjection.container.geofenceTrackerApi

    val predict: PredictApi
        get() = DependencyInjection.container.predictApi

    val deepLink: DeepLinkApi
        get() = DependencyInjection.container.deepLinkApi

}
