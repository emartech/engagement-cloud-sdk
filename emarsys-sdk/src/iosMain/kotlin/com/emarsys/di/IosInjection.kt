package com.emarsys.di

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.emarsys.EmarsysConfig
import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.cache.IosFileCache
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.db.events.IosSqDelightEventsDao
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.device.UIDevice
import com.emarsys.core.device.UIDeviceApi
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
import com.emarsys.mobileengage.pushtoinapp.PushToInAppHandler
import com.emarsys.setup.PlatformInitializer
import com.emarsys.setup.PlatformInitializerApi
import com.emarsys.setup.config.IosSdkConfigStore
import com.emarsys.setup.config.SdkConfigStoreApi
import com.emarsys.sqldelight.EmarsysDB
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIApplication
import platform.UserNotifications.UNUserNotificationCenter

object IosInjection {
    val iosModules = module {
        single<NSUserDefaults> { NSUserDefaults(StorageConstants.SUITE_NAME) }
        single<UNUserNotificationCenter> { UNUserNotificationCenter.currentNotificationCenter() }
        single<StringStorageApi> { StringStorage(userDefaults = get()) }
        single<SdkConfigStoreApi<EmarsysConfig>> {
            IosSdkConfigStore(
                typedStorage = get()
            )
        }
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
                webViewProvider = webViewProvider
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
                sdkEventFlow = get(named(EventFlowTypes.InternalEventFlow))
            )
        }
    }
}

actual fun SdkKoinIsolationContext.loadPlatformModules(): List<Module> {
    return listOf(IosInjection.iosModules)
}