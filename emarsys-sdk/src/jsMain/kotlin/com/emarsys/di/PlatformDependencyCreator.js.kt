package com.emarsys.di

import com.emarsys.api.push.PushInternalApi
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.badge.WebBadgeCountHandler
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.WebPlatformInfoCollector
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.permission.WebPermissionHandler
import com.emarsys.core.provider.ApplicationVersionProvider
import com.emarsys.core.providers.Provider
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.url.WebExternalUrlOpener
import com.emarsys.mobileengage.push.PushService
import com.emarsys.setup.PlatformInitState
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.connection.WebConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import com.emarsys.watchdog.lifecycle.WebLifeCycleWatchDog
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import web.dom.document

actual class PlatformDependencyCreator actual constructor(platformContext: PlatformContext) :
    DependencyCreator {

    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext

    override fun createStorage(): TypedStorageApi<String?> {
        return StringStorage(platformContext.storage)
    }

    override fun createDeviceInfoCollector(
        uuidProvider: Provider<String>,
        timezoneProvider: Provider<String>
    ): DeviceInfoCollector {
        return DeviceInfoCollector(
            uuidProvider,
            timezoneProvider,
            createWebDeviceInfoCollector(),
            createStorage(),
            createApplicationVersionProvider(),
        )
    }

    override fun createPlatformInitState(
        pushApi: PushInternalApi,
        sdkDispatcher: CoroutineDispatcher
    ): State {
        val pushService = PushService(
            "BDa49_IiPdIo2Kda5cATItp81sOaYg-eFFISMdlSXatDAIZCdtAxUuMVzXo4M2MXXI0sUYQzQI7shyNkKgwyD_I",
            "/ems-service-worker.js",
            pushApi
        )
        return PlatformInitState(pushService)
    }

    override fun createPermissionHandler(): PermissionHandlerApi {
        return WebPermissionHandler()
    }

    override fun createBadgeCountHandler(): BadgeCountHandlerApi {
        return WebBadgeCountHandler()
    }

    override fun createExternalUrlOpener(): ExternalUrlOpenerApi {
        return WebExternalUrlOpener()
    }

    override fun createConnectionWatchDog(sdkLogger: SdkLogger): ConnectionWatchDog {
        return WebConnectionWatchDog(window)
    }

    override fun createLifeCycleWatchDog(): LifecycleWatchDog {
        return WebLifeCycleWatchDog(document, CoroutineScope(Dispatchers.Default))
    }

    private fun createWebDeviceInfoCollector(): WebPlatformInfoCollector {
        return WebPlatformInfoCollector(getNavigatorData())
    }

    private fun createApplicationVersionProvider(): ApplicationVersionProvider {
        return ApplicationVersionProvider()
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