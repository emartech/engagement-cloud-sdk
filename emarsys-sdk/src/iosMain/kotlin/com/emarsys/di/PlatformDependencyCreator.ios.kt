package com.emarsys.di

import com.emarsys.api.AppEvent
import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.PushInternalApi
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.badge.IosBadgeCountHandler
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.cache.IosFileCache
import com.emarsys.core.channel.CustomEventChannelApi
import com.emarsys.core.clipboard.ClipboardHandlerApi
import com.emarsys.core.clipboard.IosClipboardHandler
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.UIDevice
import com.emarsys.core.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.message.MsgHubApi
import com.emarsys.core.permission.IosPermissionHandler
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.provider.IosApplicationVersionProvider
import com.emarsys.core.provider.IosLanguageProvider
import com.emarsys.core.providers.HardwareIdProvider
import com.emarsys.core.providers.Provider
import com.emarsys.core.pushtoinapp.PushToInAppHandlerApi
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
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import com.emarsys.mobileengage.inapp.InAppJsBridge
import com.emarsys.mobileengage.inapp.InAppPresenter
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewProvider
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.providers.SceneProvider
import com.emarsys.mobileengage.inapp.providers.ViewControllerProvider
import com.emarsys.mobileengage.inapp.providers.WebViewProvider
import com.emarsys.mobileengage.inapp.providers.WindowProvider
import com.emarsys.mobileengage.push.IosGathererPush
import com.emarsys.mobileengage.push.IosLoggingPush
import com.emarsys.mobileengage.push.IosPush
import com.emarsys.mobileengage.push.IosPushInstance
import com.emarsys.mobileengage.push.IosPushInternal
import com.emarsys.mobileengage.pushtoinapp.PushToInAppHandler
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.setup.PlatformInitializer
import com.emarsys.setup.PlatformInitializerApi
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UserNotifications.UNUserNotificationCenter

actual class PlatformDependencyCreator actual constructor(
    private val sdkContext: SdkContextApi,
    private val uuidProvider: Provider<String>,
    private val sdkLogger: Logger,
    private val json: Json,
    private val msgHub: MsgHubApi,
    actionHandler: ActionHandlerApi,
    eventChannel: CustomEventChannelApi
) : DependencyCreator {
    private val platformContext: IosPlatformContext = IosPlatformContext()
    private val processInfo = NSProcessInfo()
    private val uiDevice = UIDevice(processInfo)
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    actual override fun createPlatformInitializer(pushActionFactory: ActionFactoryApi<ActionModel>): PlatformInitializerApi {
        return PlatformInitializer()
    }

    actual override fun createPlatformContext(pushActionFactory: ActionFactoryApi<ActionModel>): PlatformContext {
        return platformContext
    }

    actual override fun createStorage(): TypedStorageApi<String?> {
        return StringStorage(platformContext.userDefaults)
    }

    actual override fun createDeviceInfoCollector(
        timezoneProvider: Provider<String>,
        storage: TypedStorageApi<String?>
    ): DeviceInfoCollector {
        return DeviceInfoCollector(
            HardwareIdProvider(uuidProvider, createStorage()),
            createApplicationVersionProvider(),
            createLanguageProvider(),
            timezoneProvider,
            uiDevice,
            json,
        )
    }

    actual override fun createPlatformInitState(
        pushApi: PushInternalApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>,
        downloaderApi: DownloaderApi,
        inAppDownloader: InAppDownloaderApi,
        storage: TypedStorageApi<String?>
    ): State {
        return PlatformInitState(pushApi as IosPushInstance)
    }

    actual override fun createPermissionHandler(): PermissionHandlerApi {
        return IosPermissionHandler(notificationCenter)
    }

    actual override fun createBadgeCountHandler(): BadgeCountHandlerApi {
        return IosBadgeCountHandler(notificationCenter, uiDevice, sdkContext.mainDispatcher)
    }

    actual override fun createExternalUrlOpener(): ExternalUrlOpenerApi {
        return IosExternalUrlOpener(UIApplication.sharedApplication, sdkContext.mainDispatcher)
    }

    actual override fun createPushToInAppHandler(
        inAppDownloader: InAppDownloaderApi,
        inAppHandler: InAppHandlerApi
    ): PushToInAppHandlerApi {
        return PushToInAppHandler(inAppDownloader, inAppHandler)
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

    actual override fun createClipboardHandler(): ClipboardHandlerApi {
        return IosClipboardHandler(UIPasteboard.generalPasteboard)
    }

    override fun createLaunchApplicationHandler(): LaunchApplicationHandlerApi {
        TODO("Not yet implemented")
    }

    actual override fun createPushInternal(
        pushClient: PushClientApi,
        storage: TypedStorageApi<String?>,
        pushContext: ApiContext<PushCall>,
        notificationEvents: MutableSharedFlow<AppEvent>,
        eventClient: EventClientApi,
        actionFactory: ActionFactoryApi<ActionModel>,
        json: Json,
        sdkDispatcher: CoroutineDispatcher
    ): PushInstance {
        return IosPushInternal(
            pushClient,
            storage,
            pushContext,
            sdkContext,
            notificationEvents,
            actionFactory,
            json,
            sdkDispatcher,
            sdkLogger
        )
    }

    actual override fun createPushApi(
        pushInternal: PushInstance,
        storage: TypedStorageApi<String?>,
        pushContext: ApiContext<PushCall>,
        notificationEvents: MutableSharedFlow<AppEvent>
    ): PushApi {
        val loggingPush = IosLoggingPush(sdkLogger, notificationEvents)
        val pushGatherer = IosGathererPush(pushContext, storage, notificationEvents)
        return IosPush(loggingPush, pushGatherer, pushInternal as IosPushInternal, sdkContext)
    }

}