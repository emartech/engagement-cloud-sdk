package com.emarsys.api.contact

import com.emarsys.di.SdkKoinIsolationContext.koin

class IosPublicContact: IosPublicContactApi {
    override suspend fun link(contactFieldId: Int, contactFieldValue: String) {
        koin.get<ContactApi>().linkContact(contactFieldId, contactFieldValue)
    }

    override suspend fun linkAuthenticated(contactFieldId: Int, openIdToken: String) {
        koin.get<ContactApi>().linkAuthenticatedContact(contactFieldId, openIdToken)
    }

    override suspend fun unlink() {
        koin.get<ContactApi>().unlinkContact()
    }
}