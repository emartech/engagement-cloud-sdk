package com.emarsys.api.inapp

import com.emarsys.di.SdkKoinIsolationContext.koin

class IosPublicInApp: IosPublicInAppApi {
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