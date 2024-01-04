package com.emarsys

import com.emarsys.di.DependencyInjection

object Emarsys {

    suspend fun initialize() {

    }

    suspend fun enableTracking(config: EmarsysConfig){
        config.isValid()
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

}
