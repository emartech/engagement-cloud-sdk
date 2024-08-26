package com.emarsys.di

import com.emarsys.api.push.PushInternalApi
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.IosBadgeCountHandler
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.cache.IosFileCache
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.UIDevice
import com.emarsys.core.log.Logger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.message.MsgHubApi
import com.emarsys.core.permission.IosPermissionHandler
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.provider.IosApplicationVersionProvider
import com.emarsys.core.provider.IosLanguageProvider
import com.emarsys.core.providers.HardwareIdProvider
import com.emarsys.core.providers.Provider
import com.emarsys.core.setup.PlatformInitState
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.url.IosExternalUrlOpener
import com.emarsys.core.util.DownloaderApi
import com.emarsys.core.watchdog.connection.IosConnectionWatchdog
import com.emarsys.core.watchdog.connection.NWPathMonitorWrapper
import com.emarsys.core.watchdog.lifecycle.IosLifecycleWatchdog
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.inapp.InAppJsBridge
import com.emarsys.mobileengage.inapp.InAppPresenter
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewProvider
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.providers.SceneProvider
import com.emarsys.mobileengage.inapp.providers.ViewControllerProvider
import com.emarsys.mobileengage.inapp.providers.WebViewProvider
import com.emarsys.mobileengage.inapp.providers.WindowProvider
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import platform.Foundation.NSFileManager
import platform.UIKit.UIApplication

actual class PlatformDependencyCreator actual constructor(
    platformContext: PlatformContext,
    private val sdkContext: SdkContextApi,
    private val uuidProvider: Provider<String>,
    private val sdkLogger: Logger,
    private val json: Json,
    private val msgHub: MsgHubApi
) : DependencyCreator {
    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext

    actual override fun createStorage(): TypedStorageApi<String?> {
        return StringStorage(platformContext.userDefaults)
    }

    actual override fun createDeviceInfoCollector(
        timezoneProvider: Provider<String>
    ): DeviceInfoCollector {
        return DeviceInfoCollector(
            HardwareIdProvider(uuidProvider, createStorage()),
            createApplicationVersionProvider(),
            createLanguageProvider(),
            timezoneProvider,
            UIDevice(),
            json,
        )
    }

    actual override fun createPlatformInitState(
        pushApi: PushInternalApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>,
        downloaderApi: DownloaderApi
    ): State {
        return PlatformInitState()
    }

    actual override fun createPermissionHandler(): PermissionHandlerApi {
        return IosPermissionHandler()
    }

    actual override fun createBadgeCountHandler(): BadgeCountHandlerApi {
        return IosBadgeCountHandler()
    }

    actual override fun createExternalUrlOpener(): ExternalUrlOpenerApi {
        return IosExternalUrlOpener()
    }

    actual override fun createConnectionWatchDog(sdkLogger: SdkLogger): ConnectionWatchDog {
        return IosConnectionWatchdog(NWPathMonitorWrapper(sdkContext.sdkDispatcher))
    }

    actual override fun createLifeCycleWatchDog(): LifecycleWatchDog {
        return IosLifecycleWatchdog()
    }

    actual override fun createApplicationVersionProvider(): Provider<String> {
        return IosApplicationVersionProvider()
    }

    actual override fun createLanguageProvider(): Provider<String> {
        return IosLanguageProvider()
    }

    actual override fun createFileCache(): FileCacheApi {
        return IosFileCache(NSFileManager.defaultManager)
    }

    actual override fun createInAppViewProvider(actionFactory: ActionFactoryApi<ActionModel>): InAppViewProviderApi {
        return InAppViewProvider(
            sdkContext.mainDispatcher,
            WebViewProvider(
                sdkContext.mainDispatcher,
                InAppJsBridge(
                    actionFactory,
                    json,
                    CoroutineScope(sdkContext.mainDispatcher),
                    CoroutineScope(sdkContext.sdkDispatcher),
                    sdkLogger
                )
            )
        )
    }

    actual override fun createInAppPresenter(): InAppPresenterApi {
        return InAppPresenter(
            WindowProvider(
                sceneProvider = SceneProvider(UIApplication.sharedApplication),
                viewControllerProvider = ViewControllerProvider(),
                mainDispatcher = sdkContext.mainDispatcher
            ), mainDispatcher = sdkContext.mainDispatcher,
            msgHub = msgHub
        )
    }
}