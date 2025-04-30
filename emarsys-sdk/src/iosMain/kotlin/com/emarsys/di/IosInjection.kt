package com.emarsys.di

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.emarsys.IosEmarsysConfig
import com.emarsys.api.config.IosConfig
import com.emarsys.api.config.IosConfigApi
import com.emarsys.api.contact.IosContact
import com.emarsys.api.contact.IosContactApi
import com.emarsys.api.deeplink.IosDeepLink
import com.emarsys.api.deeplink.IosDeepLinkApi
import com.emarsys.api.geofence.IosGeofence
import com.emarsys.api.geofence.IosGeofenceApi
import com.emarsys.api.inapp.IosInApp
import com.emarsys.api.inapp.IosInAppApi
import com.emarsys.api.predict.IosPredict
import com.emarsys.api.predict.IosPredictApi
import com.emarsys.api.push.IosPush
import com.emarsys.api.push.IosPushApi
import com.emarsys.api.push.PushApi
import com.emarsys.api.tracking.IosTracking
import com.emarsys.api.tracking.IosTrackingApi
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
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.device.UIDevice
import com.emarsys.core.device.UIDeviceApi
import com.emarsys.core.language.IosLanguageTagValidator
import com.emarsys.core.language.LanguageTagValidatorApi
import com.emarsys.core.launchapplication.IosLaunchApplicationHandler
import com.emarsys.core.permission.IosPermissionHandler
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.provider.IosApplicationVersionProvider
import com.emarsys.core.provider.IosLanguageProvider
import com.emarsys.core.providers.ApplicationVersionProviderApi
import com.emarsys.core.providers.ClientIdProvider
import com.emarsys.core.providers.LanguageProviderApi
import com.emarsys.core.setup.PlatformInitState
import com.emarsys.core.state.State
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StorageConstants.DB_NAME
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.url.IosExternalUrlOpener
import com.emarsys.core.watchdog.connection.IosConnectionWatchdog
import com.emarsys.core.watchdog.connection.NWPathMonitorWrapper
import com.emarsys.core.watchdog.lifecycle.IosLifecycleWatchdog
import com.emarsys.enable.PlatformInitializer
import com.emarsys.enable.PlatformInitializerApi
import com.emarsys.enable.config.IosSdkConfigStore
import com.emarsys.enable.config.SdkConfigStoreApi
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewProvider
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.IosInAppPresenter
import com.emarsys.mobileengage.inapp.providers.InAppJsBridgeProvider
import com.emarsys.mobileengage.inapp.providers.SceneProvider
import com.emarsys.mobileengage.inapp.providers.ViewControllerProvider
import com.emarsys.mobileengage.inapp.providers.WebViewProvider
import com.emarsys.mobileengage.inapp.providers.WindowProvider
import com.emarsys.mobileengage.push.IosGathererPush
import com.emarsys.mobileengage.push.IosLoggingPush
import com.emarsys.mobileengage.push.IosPushInstance
import com.emarsys.mobileengage.push.IosPushInternal
import com.emarsys.mobileengage.push.IosPushWrapper
import com.emarsys.mobileengage.push.IosPushWrapperApi
import com.emarsys.mobileengage.pushtoinapp.PushToInAppHandler
import com.emarsys.sqldelight.EmarsysDB
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module
import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UserNotifications.UNUserNotificationCenter

object IosInjection {
    val iosModules = module {
        single<NSUserDefaults> { NSUserDefaults(StorageConstants.SUITE_NAME) }
        single<IosContactApi> { IosContact() }
        single<IosPushApi> { IosPush() }
        single<IosTrackingApi> { IosTracking() }
        single<IosInAppApi> { IosInApp() }
        single<IosConfigApi> { IosConfig() }
        single<IosGeofenceApi> { IosGeofence() }
        single<IosPredictApi> { IosPredict() }
        single<IosDeepLinkApi> { IosDeepLink() }
        single<UNUserNotificationCenter> { UNUserNotificationCenter.currentNotificationCenter() }
        single<StringStorageApi> { StringStorage(userDefaults = get()) }
        single<SdkConfigStoreApi<IosEmarsysConfig>> { IosSdkConfigStore(typedStorage = get()) }
        single<PermissionHandlerApi> { IosPermissionHandler(notificationCenter = get()) }
        single<UIDeviceApi> { UIDevice(NSProcessInfo()) }
        single<DeviceInfoCollectorApi> {
            DeviceInfoCollector(
                clientIdProvider = ClientIdProvider(uuidProvider = get(), storage = get()),
                applicationVersionProvider = get(),
                languageProvider = get(),
                timezoneProvider = get(),
                deviceInformation = get(),
                wrapperInfoStorage = get(),
                json = get(),
                stringStorage = get(),
                sdkContext = get()
            )
        }
        single<State>(named(StateTypes.PlatformInit)) { PlatformInitState() }
        single<EventsDaoApi> {
            val driver = NativeSqliteDriver(EmarsysDB.Schema, DB_NAME)
            IosSqDelightEventsDao(
                db = EmarsysDB(driver), json = get()
            )
        }
        single<ApplicationVersionProviderApi> { IosApplicationVersionProvider() }
        single<LanguageProviderApi> { IosLanguageProvider() }
        single<PlatformInitializerApi> { PlatformInitializer() }
        single<ExternalUrlOpenerApi> {
            IosExternalUrlOpener(
                uiApplication = UIApplication.sharedApplication,
                mainDispatcher = get(named(DispatcherTypes.Main)),
                sdkDispatcher = get(named(DispatcherTypes.Sdk)),
                sdkLogger = get { parametersOf(IosExternalUrlOpener::class.simpleName) }
            )
        }
        single<PushToInAppHandlerApi> {
            PushToInAppHandler(
                downloader = get(),
                inAppHandler = get()
            )
        }
        single<ConnectionWatchDog> {
            IosConnectionWatchdog(
                NWPathMonitorWrapper(
                    sdkDispatcher = get(named(DispatcherTypes.Sdk))
                )
            )
        }
        single<LifecycleWatchDog> { IosLifecycleWatchdog() }
        single<FileCacheApi> { IosFileCache(NSFileManager.defaultManager) }
        single<InAppViewProviderApi> {
            val webViewProvider = WebViewProvider(
                mainDispatcher = get(named(DispatcherTypes.Main)),
                InAppJsBridgeProvider(
                    actionFactory = get<EventActionFactoryApi>(),
                    json = get(),
                    mainDispatcher = get(named(DispatcherTypes.Main)),
                    sdkDispatcher = get(named(DispatcherTypes.Sdk)),
                    sdkLogger = get { parametersOf(InAppJsBridgeProvider::class.simpleName) }
                )
            )
            InAppViewProvider(
                mainDispatcher = get(named(DispatcherTypes.Main)),
                webViewProvider = webViewProvider,
                timestampProvider = get(),
            )
        }
        single<InAppPresenterApi> {
            val windowProvider = WindowProvider(
                sceneProvider = SceneProvider(UIApplication.sharedApplication),
                viewControllerProvider = ViewControllerProvider(),
                mainDispatcher = get(named(DispatcherTypes.Main))
            )
            IosInAppPresenter(
                windowProvider = windowProvider,
                mainDispatcher = get(named(DispatcherTypes.Main)),
                sdkDispatcher = get(named(DispatcherTypes.Sdk)),
                sdkEventDistributor = get(),
                logger = get { parametersOf(IosInAppPresenter::class.simpleName) }
            )
        }
        single<ClipboardHandlerApi> { IosClipboardHandler(UIPasteboard.generalPasteboard) }
        single<LaunchApplicationHandlerApi> { IosLaunchApplicationHandler() }
        single<LanguageTagValidatorApi> { IosLanguageTagValidator() }
        single<SdkConfigStoreApi<IosEmarsysConfig>> { IosSdkConfigStore(typedStorage = get()) }
        single<IosPushInstance>(named(InstanceType.Internal)) {
            val badgeCountHandler = IosBadgeCountHandler(
                notificationCenter = get(),
                uiDevice = get(),
                mainCoroutineDispatcher = get(
                    named(DispatcherTypes.Main)
                )
            )
            IosPushInternal(
                storage = get(),
                pushContext = get(),
                sdkContext = get(),
                actionFactory = get(),
                actionHandler = get(),
                badgeCountHandler = badgeCountHandler,
                json = get(),
                sdkDispatcher = get(named(DispatcherTypes.Sdk)),
                sdkLogger = get { parametersOf(IosPushInternal::class.simpleName) },
                sdkEventDistributor = get(),
                timestampProvider = get(),
                uuidProvider = get()
            )
        }
        single<IosPushInstance>(named(InstanceType.Gatherer)) {
            IosGathererPush(
                storage = get(),
                iosPushInternal = get(named(InstanceType.Internal)),
                context = get()
            )
        }
        single<IosPushInstance>(named(InstanceType.Logging)) {
            IosLoggingPush(
                storage = get(),
                sdkDispatcher = get(named(DispatcherTypes.Sdk)),
                logger = get { parametersOf(IosLoggingPush::class.simpleName) }
            )
        }
        single<PushApi> {
            IosPushWrapper(
                loggingApi = get(named(InstanceType.Logging)),
                gathererApi = get(named(InstanceType.Gatherer)),
                internalApi = get(named(InstanceType.Internal)),
                sdkContext = get(),
                sdkLogger = get { parametersOf(IosPushWrapper::class.simpleName) }
            )
        } binds arrayOf(
            IosPushWrapperApi::class,
            PushApi::class
        )
    }
}

actual fun SdkKoinIsolationContext.loadPlatformModules(): List<Module> {
    return listOf(IosInjection.iosModules)
}