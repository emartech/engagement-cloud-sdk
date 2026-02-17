package com.sap.ec.api.contact

import com.sap.ec.di.SdkKoinIsolationContext.koin

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