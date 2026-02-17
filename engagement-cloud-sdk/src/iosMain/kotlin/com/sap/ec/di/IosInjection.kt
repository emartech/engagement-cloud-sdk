package com.sap.ec.di

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.sap.ec.IosEngagementCloudSDKConfig
import com.sap.ec.api.config.IosConfig
import com.sap.ec.api.config.IosConfigApi
import com.sap.ec.api.contact.IosContact
import com.sap.ec.api.contact.IosContactApi
import com.sap.ec.api.deeplink.IosDeepLink
import com.sap.ec.api.deeplink.IosDeepLinkApi
import com.sap.ec.api.embeddedmessaging.IosEmbeddedMessaging
import com.sap.ec.api.embeddedmessaging.IosEmbeddedMessagingApi
import com.sap.ec.api.inapp.IosInApp
import com.sap.ec.api.inapp.IosInAppApi
import com.sap.ec.api.push.IosPush
import com.sap.ec.api.push.IosPushApi
import com.sap.ec.api.push.PushApi
import com.sap.ec.api.setup.IosSetup
import com.sap.ec.api.setup.IosSetupApi
import com.sap.ec.api.tracking.IosTracking
import com.sap.ec.api.tracking.IosTrackingApi
import com.sap.ec.core.actions.clipboard.ClipboardHandlerApi
import com.sap.ec.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.sap.ec.core.badge.IosBadgeCountHandler
import com.sap.ec.core.cache.FileCacheApi
import com.sap.ec.core.cache.IosFileCache
import com.sap.ec.core.clipboard.IosClipboardHandler
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.db.events.IosSqDelightEventsDao
import com.sap.ec.core.device.DeviceInfoCollector
import com.sap.ec.core.device.DeviceInfoCollectorApi
import com.sap.ec.core.device.UIDevice
import com.sap.ec.core.device.UIDeviceApi
import com.sap.ec.core.device.notification.IosNotificationSettingsCollector
import com.sap.ec.core.device.notification.IosNotificationSettingsCollectorApi
import com.sap.ec.core.language.IosLanguageTagValidator
import com.sap.ec.core.language.LanguageTagValidatorApi
import com.sap.ec.core.launchapplication.IosLaunchApplicationHandler
import com.sap.ec.core.permission.IosPermissionHandler
import com.sap.ec.core.permission.PermissionHandlerApi
import com.sap.ec.core.providers.ApplicationVersionProviderApi
import com.sap.ec.core.providers.ClientIdProvider
import com.sap.ec.core.providers.IosApplicationVersionProvider
import com.sap.ec.core.providers.IosLanguageProvider
import com.sap.ec.core.providers.LanguageProviderApi
import com.sap.ec.core.providers.pagelocation.PageLocationProvider
import com.sap.ec.core.providers.pagelocation.PageLocationProviderApi
import com.sap.ec.core.providers.platform.PlatformCategoryProvider
import com.sap.ec.core.providers.platform.PlatformCategoryProviderApi
import com.sap.ec.core.setup.PlatformInitState
import com.sap.ec.core.state.State
import com.sap.ec.core.storage.KeychainStorage
import com.sap.ec.core.storage.StorageConstants
import com.sap.ec.core.storage.StorageConstants.DB_NAME
import com.sap.ec.core.storage.StringStorage
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.core.url.ExternalUrlOpenerApi
import com.sap.ec.core.url.IosExternalUrlOpener
import com.sap.ec.core.watchdog.connection.IosConnectionWatchdog
import com.sap.ec.core.watchdog.connection.NWPathMonitorWrapper
import com.sap.ec.core.watchdog.lifecycle.IosLifecycleWatchdog
import com.sap.ec.enable.PlatformInitializer
import com.sap.ec.enable.PlatformInitializerApi
import com.sap.ec.enable.config.IosSdkConfigStore
import com.sap.ec.enable.config.SdkConfigStoreApi
import com.sap.ec.init.states.LegacySDKMigrationState
import com.sap.ec.mobileengage.action.EventActionFactoryApi
import com.sap.ec.mobileengage.inapp.InAppViewProvider
import com.sap.ec.mobileengage.inapp.IosInAppPresenter
import com.sap.ec.mobileengage.inapp.IosInlineInAppViewRenderer
import com.sap.ec.mobileengage.inapp.presentation.InAppPresenterApi
import com.sap.ec.mobileengage.inapp.presentation.InlineInAppViewRendererApi
import com.sap.ec.mobileengage.inapp.providers.InAppJsBridgeFactory
import com.sap.ec.mobileengage.inapp.providers.IosWebViewFactory
import com.sap.ec.mobileengage.inapp.providers.SceneProvider
import com.sap.ec.mobileengage.inapp.providers.ViewControllerProvider
import com.sap.ec.mobileengage.inapp.providers.WindowProvider
import com.sap.ec.mobileengage.inapp.view.InAppViewProviderApi
import com.sap.ec.mobileengage.push.IosGathererPush
import com.sap.ec.mobileengage.push.IosLoggingPush
import com.sap.ec.mobileengage.push.IosPushInstance
import com.sap.ec.mobileengage.push.IosPushInternal
import com.sap.ec.mobileengage.push.IosPushWrapper
import com.sap.ec.mobileengage.push.IosPushWrapperApi
import com.sap.ec.sqldelight.SapEngagementCloudDB
import com.sap.ec.watchdog.connection.ConnectionWatchDog
import com.sap.ec.watchdog.lifecycle.LifecycleWatchDog
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
        single<IosSetupApi> { IosSetup(get()) }
        single<IosPushApi> { IosPush() }
        single<IosTrackingApi> { IosTracking() }
        single<IosInAppApi> { IosInApp() }
        single<IosConfigApi> {
            IosConfig(
                configApi = get(),
                iosNotificationSettingsCollector = get()
            )
        }
        single<IosDeepLinkApi> { IosDeepLink() }
        single<UNUserNotificationCenter> { UNUserNotificationCenter.currentNotificationCenter() }
        single<StringStorageApi> { StringStorage(userDefaults = get()) }
        single<SdkConfigStoreApi<IosEngagementCloudSDKConfig>> { IosSdkConfigStore(typedStorage = get()) }
        single<PermissionHandlerApi> { IosPermissionHandler(notificationCenter = get()) }
        single<UIDeviceApi> { UIDevice(NSProcessInfo()) }
        single<IosNotificationSettingsCollectorApi> {
            IosNotificationSettingsCollector(
                json = get()
            )
        }
        single<PageLocationProviderApi> { PageLocationProvider() }
        single<PlatformCategoryProviderApi> { PlatformCategoryProvider() }
        single<DeviceInfoCollectorApi> {
            DeviceInfoCollector(
                clientIdProvider = ClientIdProvider(uuidProvider = get(), storage = get()),
                applicationVersionProvider = get(),
                languageProvider = get(),
                timezoneProvider = get(),
                deviceInformation = get(),
                wrapperInfoStorage = get(),
                iosNotificationSettingsCollector = get(),
                json = get(),
                stringStorage = get(),
                sdkContext = get(),
                platformCategoryProvider = get()
            )
        }
        single<State>(named(InitStateTypes.LegacySDKMigration)) {
            LegacySDKMigrationState(
                requestContext = get(),
                sdkContext = get(),
                stringStorage = get(),
                keychainStorage = KeychainStorage(),
                sdkLogger = get { parametersOf(LegacySDKMigrationState::class.simpleName) }
            )
        }
        single<State>(named(StateTypes.PlatformInit)) { PlatformInitState() }
        single<EventsDaoApi> {
            val driver = NativeSqliteDriver(SapEngagementCloudDB.Schema, DB_NAME)
            IosSqDelightEventsDao(
                db = SapEngagementCloudDB(driver), json = get()
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
            val inAppJsBridgeFactory = InAppJsBridgeFactory(
                actionFactory = get<EventActionFactoryApi>(),
                json = get(),
                mainDispatcher = get(named(DispatcherTypes.Main)),
                sdkDispatcher = get(named(DispatcherTypes.Sdk)),
                sdkLogger = get { parametersOf(InAppJsBridgeFactory::class.simpleName) }
            )
            val iosWebViewFactory = IosWebViewFactory(
                mainDispatcher = get(named(DispatcherTypes.Main)),
                inAppJsBridgeFactory = inAppJsBridgeFactory
            )
            InAppViewProvider(
                mainDispatcher = get(named(DispatcherTypes.Main)),
                webViewProvider = iosWebViewFactory,
                timestampProvider = get(),
                contentReplacer = get()
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
        single<InlineInAppViewRendererApi> { IosInlineInAppViewRenderer() }
        single<LanguageTagValidatorApi> { IosLanguageTagValidator() }
        single<SdkConfigStoreApi<IosEngagementCloudSDKConfig>> { IosSdkConfigStore(typedStorage = get()) }
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
                context = get(),
                storage = get(),
                iosPushInternal = get(named(InstanceType.Internal)),
                sdkContext = get()
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
        single<IosEmbeddedMessagingApi> { IosEmbeddedMessaging(embeddedMessaging = get()) }
    }
}

actual fun SdkKoinIsolationContext.loadPlatformModules(): List<Module> {
    return listOf(IosInjection.iosModules)
}