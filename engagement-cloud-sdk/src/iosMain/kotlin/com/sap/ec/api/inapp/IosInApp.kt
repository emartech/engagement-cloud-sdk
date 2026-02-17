package com.sap.ec.api.inapp

import com.sap.ec.di.SdkKoinIsolationContext.koin

class IosInApp: IosInAppApi {
    override val isPaused: Boolean
        get() {
            return koin.get<InAppApi>().isPaused
        }

    override suspend fun pause() {
        koin.get<InAppApi>().pause()
    }

    override suspend fun resume() {
        koin.get<InAppApi>().resume()
    }
}