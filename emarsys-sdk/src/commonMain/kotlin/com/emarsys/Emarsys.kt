package com.emarsys

import com.emarsys.api.event.CustomEvent
import com.emarsys.di.DependencyInjection

object Emarsys {

    suspend fun initialize() {
        DependencyInjection.container
    }

    suspend fun enableTracking(config: EmarsysConfig) {
        config.isValid()
        DependencyInjection.container?.setupOrganizerApi?.setup(config)
    }

    suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        DependencyInjection.container?.contactApi?.linkContact(contactFieldId, contactFieldValue)
    }

    suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        DependencyInjection.container?.contactApi?.linkAuthenticatedContact(contactFieldId, openIdToken)
    }

    suspend fun unlinkContact() {
        DependencyInjection.container?.contactApi?.unlinkContact()
    }

    suspend fun trackCustomEvent(event: String, attributes: Map<String, String>) {
        DependencyInjection.container?.eventTrackerApi?.trackEvent(CustomEvent(event, attributes))
    }
}
