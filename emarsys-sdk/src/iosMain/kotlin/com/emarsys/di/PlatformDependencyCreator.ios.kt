package com.emarsys.di

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.emarsys.SdkConfig
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushInstance
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.actions.badge.BadgeCountHandlerApi
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.badge.IosBadgeCountHandler
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.cache.IosFileCache
import com.emarsys.core.clipboard.IosClipboardHandler
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.db.events.IosSqDelightEventsDao
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.UIDevice
import com.emarsys.core.language.LanguageTagValidator
import com.emarsys.core.language.LanguageTagValidatorApi
import com.emarsys.core.launchapplication.IosLaunchApplicationHandler
import com.emarsys.core.log.Logger
import com.emarsys.core.permission.IosPermissionHandler
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.provider.IosApplicationVersionProvider
import com.emarsys.core.provider.IosLanguageProvider
import com.emarsys.core.providers.ApplicationVersionProviderApi
import com.emarsys.core.providers.ClientIdProvider
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.LanguageProviderApi
import com.emarsys.core.providers.TimezoneProviderApi
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.setup.PlatformInitState
import com.emarsys.core.state.State
import com.emarsys.core.storage.StorageConstants.DB_NAME
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.url.IosExternalUrlOpener
import com.emarsys.core.watchdog.connection.IosConnectionWatchdog
import com.emarsys.core.watchdog.connection.NWPathMonitorWrapper
import com.emarsys.core.watchdog.lifecycle.IosLifecycleWatchdog
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import com.emarsys.mobileengage.inapp.InAppPresenter
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewProvider
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.providers.InAppJsBridgeProvider
import com.emarsys.mobileengage.inapp.providers.SceneProvider
import com.emarsys.mobileengage.inapp.providers.ViewControllerProvider
import com.emarsys.mobileengage.inapp.providers.WebViewProvider
import com.emarsys.mobileengage.inapp.providers.WindowProvider
import com.emarsys.mobileengage.push.IosGathererPush
import com.emarsys.mobileengage.push.IosLoggingPush
import com.emarsys.mobileengage.push.IosPush
import com.emarsys.mobileengage.push.IosPushInternal
import com.emarsys.mobileengage.pushtoinapp.PushToInAppHandler
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.setup.config.IosSdkConfigStore
import com.emarsys.setup.config.SdkConfigStoreApi
import com.emarsys.sqldelight.EmarsysDB
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UserNotifications.UNUserNotificationCenter

internal actual class PlatformDependencyCreator actual constructor(
    private val sdkContext: SdkContextApi,
    private val uuidProvider: UuidProviderApi,
    private val sdkLogger: Logger,
    private val json: Json,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val actionHandler: ActionHandlerApi,
    private val timestampProvider: InstantProvider,
) : DependencyCreator, SdkComponent {
    private val processInfo = NSProcessInfo()
    private val uiDevice = UIDevice(processInfo)
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    private val badgeCountHandler: BadgeCountHandlerApi =
        IosBadgeCountHandler(notificationCenter, uiDevice, sdkContext.mainDispatcher)
    private val userDefaults: NSUserDefaults by inject()

    private val stringStorage: StringStorageApi by lazy {
        StringStorage(userDefaults)
    }

    actual override fun createEventsDao(): EventsDaoApi {
        val driver = NativeSqliteDriver(EmarsysDB.Schema, DB_NAME)
        return IosSqDelightEventsDao(EmarsysDB(driver), json)
    }

    actual override fun createDeviceInfoCollector(
        timezoneProvider: TimezoneProviderApi,
        typedStorage: TypedStorageApi
    ): DeviceInfoCollector {
        return DeviceInfoCollector(
            ClientIdProvider(uuidProvider, stringStorage),
            createApplicationVersionProvider(),
            createLanguageProvider(),
            timezoneProvider,
            uiDevice,
            typedStorage,
            json,
            stringStorage,
            sdkContext
        )
    }

    actual override fun createPlatformInitState(
        pushApi: PushApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContextApi,
        actionFactory: EventActionFactoryApi,
        storage: StringStorageApi
    ): State {
        return PlatformInitState()
    }

    actual override fun createPermissionHandler(): PermissionHandlerApi {
        return IosPermissionHandler(notificationCenter)
    }

    actual override fun createExternalUrlOpener(): ExternalUrlOpenerApi {
        return IosExternalUrlOpener(
            UIApplication.sharedApplication,
            sdkContext.mainDispatcher,
            sdkContext.sdkDispatcher,
            sdkLogger
        )
    }

    actual override fun createPushToInAppHandler(
        inAppDownloader: InAppDownloaderApi,
        inAppHandler: InAppHandlerApi
    ): PushToInAppHandlerApi {
        return PushToInAppHandler(inAppDownloader, inAppHandler)
    }

    actual override fun createConnectionWatchDog(sdkLogger: Logger): ConnectionWatchDog {
        return IosConnectionWatchdog(NWPathMonitorWrapper(sdkContext.sdkDispatcher))
    }

    actual override fun createLifeCycleWatchDog(): LifecycleWatchDog {
        return IosLifecycleWatchdog()
    }

    actual override fun createApplicationVersionProvider(): ApplicationVersionProviderApi {
        return IosApplicationVersionProvider()
    }

    actual override fun createLanguageProvider(): LanguageProviderApi {
        return IosLanguageProvider()
    }

    actual override fun createFileCache(): FileCacheApi {
        return IosFileCache(NSFileManager.defaultManager)
    }

    actual override fun createInAppViewProvider(eventActionFactory: EventActionFactoryApi): InAppViewProviderApi {
        return InAppViewProvider(
            sdkContext.mainDispatcher,
            WebViewProvider(
                sdkContext.mainDispatcher,
                InAppJsBridgeProvider(
                    eventActionFactory,
                    json,
                    sdkContext.mainDispatcher,
                    sdkContext.sdkDispatcher,
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
            ),
            sdkContext.mainDispatcher,
            sdkContext.sdkDispatcher,
            sdkEventFlow,
        )
    }

    actual override fun createClipboardHandler(): ClipboardHandlerApi {
        return IosClipboardHandler(UIPasteboard.generalPasteboard)
    }

    actual override fun createLaunchApplicationHandler(): LaunchApplicationHandlerApi {
        return IosLaunchApplicationHandler()
    }

    actual override fun createLanguageTagValidator(): LanguageTagValidatorApi {
        return LanguageTagValidator()
    }

    actual override fun createPushInternal(
        pushClient: PushClientApi,
        storage: StringStorageApi,
        pushContext: PushContextApi,
        eventClient: EventClientApi,
        pushActionFactory: PushActionFactoryApi,
        json: Json,
        sdkDispatcher: CoroutineDispatcher
    ): PushInstance {
        return IosPushInternal(
            pushClient,
            storage,
            pushContext,
            sdkContext,
            pushActionFactory,
            actionHandler,
            badgeCountHandler,
            json,
            sdkDispatcher,
            sdkLogger,
            sdkEventFlow,
            timestampProvider,
            uuidProvider
        )
    }

    actual override fun createPushApi(
        pushInternal: PushInstance,
        storage: StringStorageApi,
        pushContext: PushContextApi,
    ): PushApi {
        val loggingPush = IosLoggingPush(sdkLogger, storage, sdkContext.sdkDispatcher)
        val pushGatherer = IosGathererPush(pushContext, storage, pushInternal as IosPushInternal)
        return IosPush(
            loggingPush,
            pushGatherer,
            pushInternal,
            sdkContext,
            sdkLogger
        )
    }

    actual override fun createSdkConfigStore(typedStorage: TypedStorageApi): SdkConfigStoreApi<SdkConfig> {
        return IosSdkConfigStore(typedStorage)
    }

}