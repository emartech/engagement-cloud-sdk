package com.sap.ec.api.contact

import com.sap.ec.di.SdkKoinIsolationContext.koin

class IosContact : IosContactApi {
    override suspend fun link(contactFieldValue: String) {
        koin.get<ContactApi>().link(contactFieldValue).getOrThrow()
    }

    override suspend fun linkAuthenticated(openIdToken: String) {
        koin.get<ContactApi>().linkAuthenticated(openIdToken).getOrThrow()
    }

    override suspend fun unlink() {
        koin.get<ContactApi>().unlink()
    }
}