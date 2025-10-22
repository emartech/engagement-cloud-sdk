package com.emarsys.api.contact

import com.emarsys.di.SdkKoinIsolationContext.koin

class IosContact : IosContactApi {
    override suspend fun link(contactFieldValue: String) {
        koin.get<ContactApi>().link(contactFieldValue)
    }

    override suspend fun linkAuthenticated(openIdToken: String) {
        koin.get<ContactApi>().linkAuthenticated(openIdToken)
    }

    override suspend fun unlink() {
        koin.get<ContactApi>().unlink()
    }
}