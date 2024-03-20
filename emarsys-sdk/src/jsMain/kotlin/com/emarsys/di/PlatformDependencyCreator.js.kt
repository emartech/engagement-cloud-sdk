package com.emarsys.di

import com.emarsys.api.push.PushInternalApi
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.WebPlatformInfoCollector
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.providers.Provider
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.url.WebExternalUrlOpener
import com.emarsys.mobileengage.push.PushService
import com.emarsys.setup.PlatformInitState
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher

actual class PlatformDependencyCreator actual constructor(platformContext: PlatformContext) : DependencyCreator {

    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext

    override fun createStorage(): TypedStorageApi<String?> {
        return StringStorage(platformContext.storage)
    }

    override fun createDeviceInfoCollector(uuidProvider: Provider<String>): DeviceInfoCollector {
        return DeviceInfoCollector(createWebDeviceInfoCollector(), uuidProvider, createStorage())
    }

    override fun createPlatformInitState(pushApi: PushInternalApi, sdkDispatcher: CoroutineDispatcher): State {
        val pushService = PushService(
            "BDa49_IiPdIo2Kda5cATItp81sOaYg-eFFISMdlSXatDAIZCdtAxUuMVzXo4M2MXXI0sUYQzQI7shyNkKgwyD_I",
            "/ems-service-worker.js",
            pushApi
        )
        return PlatformInitState(pushService)
    }

    override fun createPermissionHandler(): PermissionHandlerApi {
        TODO("Not yet implemented")
    }

    override fun createBadgeCountHandler(): BadgeCountHandlerApi {
        TODO("Not yet implemented")
    }

    override fun createExternalUrlOpener(): ExternalUrlOpenerApi {
        return WebExternalUrlOpener()
    }

    private fun createWebDeviceInfoCollector(): WebPlatformInfoCollector {
        return WebPlatformInfoCollector(getNavigatorData())
    }

    private fun getNavigatorData(): String {
        return listOf(
            window.navigator.platform,
            window.navigator.userAgent,
            window.navigator.appVersion,
            window.navigator.vendor,
        ).joinToString(" ")
    }

}