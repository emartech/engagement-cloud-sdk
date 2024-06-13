package com.emarsys.di

import com.emarsys.api.push.PushInternalApi
import com.emarsys.context.SdkContext
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.badge.WebBadgeCountHandler
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.WebPlatformInfoCollector
import com.emarsys.core.log.Logger
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
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.push.PushMessageMapper
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.PushService
import com.emarsys.mobileengage.push.PushServiceContext
import com.emarsys.setup.PlatformInitState
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.connection.WebConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import com.emarsys.watchdog.lifecycle.WebLifeCycleWatchDog
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import web.dom.document

actual class PlatformDependencyCreator actual constructor(platformContext: PlatformContext, sdkLogger: Logger, json: Json) :
    DependencyCreator {

    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext

    actual override fun createStorage(): TypedStorageApi<String?> {
        return StringStorage(platformContext.storage)
    }

    actual override fun createDeviceInfoCollector(
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

    actual override fun createPlatformInitState(
        pushApi: PushInternalApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>
    ): State {
        val pushPresenter = PushMessagePresenter(pushServiceContext, actionFactory, sdkDispatcher)
        val pushService = PushService(pushServiceContext, pushApi, pushMessageMapper, pushPresenter, sdkDispatcher)
        return PlatformInitState(pushService, sdkContext)
    }

    actual override fun createPermissionHandler(): PermissionHandlerApi {
        return WebPermissionHandler()
    }

    actual override fun createBadgeCountHandler(): BadgeCountHandlerApi {
        return WebBadgeCountHandler()
    }

    actual override fun createExternalUrlOpener(): ExternalUrlOpenerApi {
        return WebExternalUrlOpener()
    }

    actual override fun createConnectionWatchDog(sdkLogger: SdkLogger): ConnectionWatchDog {
        return WebConnectionWatchDog(window)
    }

    actual override fun createLifeCycleWatchDog(): LifecycleWatchDog {
        return WebLifeCycleWatchDog(document, CoroutineScope(Dispatchers.Default))
    }

    private val pushServiceContext: PushServiceContext by lazy {
        PushServiceContext()
    }

    private val pushMessageMapper: PushMessageMapper by lazy {
        PushMessageMapper()
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