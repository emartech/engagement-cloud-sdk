package com.emarsys.api.contact

import com.emarsys.di.SdkKoinIsolationContext.koin

class IosContact: IosContactApi {
    override suspend fun link(contactFieldId: Int, contactFieldValue: String) {
        koin.get<ContactApi>().link(contactFieldId, contactFieldValue)
    }

    override suspend fun linkAuthenticated(contactFieldId: Int, openIdToken: String) {
        koin.get<ContactApi>().linkAuthenticated(contactFieldId, openIdToken)
    }

    override suspend fun unlink() {
        koin.get<ContactApi>().unlink()
    }
}