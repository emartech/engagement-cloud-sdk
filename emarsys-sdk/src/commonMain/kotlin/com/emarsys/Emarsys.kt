package com.emarsys

import com.emarsys.api.config.ConfigApi
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.geofence.GeofenceTrackerApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.oneventaction.OnEventActionApi
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.push.PushApi
import com.emarsys.di.DependencyInjection

object Emarsys {

    suspend fun initialize() {
        DependencyInjection.container.setup()
    }

    suspend fun enableTracking(config: EmarsysConfig) {
        config.isValid()
        DependencyInjection.container.setupOrganizerApi.setup(config)
    }

    suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        DependencyInjection.container.contactApi.linkContact(contactFieldId, contactFieldValue)
    }

    suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        DependencyInjection.container.contactApi.linkAuthenticatedContact(contactFieldId, openIdToken)
    }

    suspend fun unlinkContact() {
        DependencyInjection.container.contactApi.unlinkContact()
    }

    suspend fun trackCustomEvent(event: String, attributes: Map<String, String>? = null) {
        DependencyInjection.container.eventTrackerApi.trackEvent(CustomEvent(event, attributes))
    }

    suspend fun trackDeepLink() {
        TODO("Not yet implemented")
    }

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

    val onEventAction: OnEventActionApi
        get() = DependencyInjection.container.onEventActionApi

    val predict: PredictApi
        get() = DependencyInjection.container.predictApi

}
