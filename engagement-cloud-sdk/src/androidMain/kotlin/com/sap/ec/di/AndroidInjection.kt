package com.sap.ec.di

import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.sap.ec.api.config.AndroidConfig
import com.sap.ec.api.config.AndroidConfigApi
import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig
import com.sap.ec.api.deeplink.AndroidDeepLink
import com.sap.ec.api.deeplink.AndroidDeepLinkApi
import com.sap.ec.api.deeplink.DeepLinkApi
import com.sap.ec.api.push.LoggingPush
import com.sap.ec.api.push.Push
import com.sap.ec.api.push.PushApi
import com.sap.ec.api.push.PushGatherer
import com.sap.ec.api.push.PushInstance
import com.sap.ec.api.push.PushInternal
import com.sap.ec.api.setup.AndroidSetup
import com.sap.ec.api.setup.AndroidSetupApi
import com.sap.ec.applicationContext
import com.sap.ec.core.actions.clipboard.ClipboardHandlerApi
import com.sap.ec.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.sap.ec.core.cache.AndroidFileCache
import com.sap.ec.core.cache.FileCacheApi
import com.sap.ec.core.db.events.AndroidSqlDelightEventsDao
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.device.AndroidLanguageProvider
import com.sap.ec.core.device.DeviceInfoCollector
import com.sap.ec.core.device.DeviceInfoCollectorApi
import com.sap.ec.core.device.PlatformInfoCollector
import com.sap.ec.core.device.PlatformInfoCollectorApi
import com.sap.ec.core.device.notification.AndroidNotificationSettingsCollector
import com.sap.ec.core.device.notification.AndroidNotificationSettingsCollectorApi
import com.sap.ec.core.language.AndroidLanguageTagValidator
import com.sap.ec.core.language.LanguageTagValidatorApi
import com.sap.ec.core.launchapplication.LaunchApplicationHandler
import com.sap.ec.core.permission.PermissionHandlerApi
import com.sap.ec.core.providers.AndroidApplicationVersionProvider
import com.sap.ec.core.providers.ApplicationVersionProviderApi
import com.sap.ec.core.providers.ClientIdProvider
import com.sap.ec.core.providers.LanguageProviderApi
import com.sap.ec.core.providers.pagelocation.PageLocationProvider
import com.sap.ec.core.providers.pagelocation.PageLocationProviderApi
import com.sap.ec.core.providers.platform.PlatformCategoryProvider
import com.sap.ec.core.providers.platform.PlatformCategoryProviderApi
import com.sap.ec.core.resource.MetadataReader
import com.sap.ec.core.state.State
import com.sap.ec.core.storage.StorageConstants
import com.sap.ec.core.storage.StorageConstants.DB_NAME
import com.sap.ec.core.storage.StringStorage
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.core.url.ExternalUrlOpenerApi
import com.sap.ec.db_migration.LegacyDBOpenHelper
import com.sap.ec.db_migration.LegacySharedPreferencesWrapper
import com.sap.ec.db_migration.LegacySharedPreferencesWrapper.Companion.EMARSYS_SECURE_SHARED_PREFERENCES_V3_NAME
import com.sap.ec.db_migration.SharedPreferenceCrypto
import com.sap.ec.enable.PlatformInitState
import com.sap.ec.enable.PlatformInitializer
import com.sap.ec.enable.PlatformInitializerApi
import com.sap.ec.enable.config.AndroidSdkConfigStore
import com.sap.ec.enable.config.SdkConfigStoreApi
import com.sap.ec.init.states.LegacySDKMigrationState
import com.sap.ec.mobileengage.action.EventActionFactoryApi
import com.sap.ec.mobileengage.clipboard.AndroidClipboardHandler
import com.sap.ec.mobileengage.inapp.AndroidInlineInAppViewRenderer
import com.sap.ec.mobileengage.inapp.InAppJsBridgeFactory
import com.sap.ec.mobileengage.inapp.InAppPresenter
import com.sap.ec.mobileengage.inapp.presentation.InAppPresenterApi
import com.sap.ec.mobileengage.inapp.presentation.InlineInAppViewRendererApi
import com.sap.ec.mobileengage.inapp.provider.InAppDialogProvider
import com.sap.ec.mobileengage.inapp.provider.InAppDialogProviderApi
import com.sap.ec.mobileengage.inapp.provider.InAppViewProvider
import com.sap.ec.mobileengage.inapp.provider.WebViewProvider
import com.sap.ec.mobileengage.inapp.view.InAppViewProviderApi
import com.sap.ec.mobileengage.permission.AndroidPermissionHandler
import com.sap.ec.mobileengage.push.AndroidPushMessageFactory
import com.sap.ec.mobileengage.push.NotificationCompatStyler
import com.sap.ec.mobileengage.push.NotificationIntentProcessor
import com.sap.ec.mobileengage.push.PushMessagePresenter
import com.sap.ec.mobileengage.push.SilentPushMessageHandler
import com.sap.ec.mobileengage.push.mapper.AndroidPushV2Mapper
import com.sap.ec.mobileengage.push.mapper.HuaweiPushV2Mapper
import com.sap.ec.mobileengage.push.mapper.SilentAndroidPushV2Mapper
import com.sap.ec.mobileengage.push.mapper.SilentHuaweiPushV2Mapper
import com.sap.ec.mobileengage.url.AndroidExternalUrlOpener
import com.sap.ec.sqldelight.SapEngagementCloudDB
import com.sap.ec.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import com.sap.ec.watchdog.connection.AndroidConnectionWatchDog
import com.sap.ec.watchdog.connection.ConnectionWatchDog
import com.sap.ec.watchdog.lifecycle.AndroidLifecycleWatchDog
import com.sap.ec.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okio.FileSystem
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.lang.reflect.Method
import java.util.Locale

object AndroidInjection {
    val androidModules = module {
        single<MetadataReader> { MetadataReader(applicationContext) }
        single<PlatformInfoCollectorApi> { PlatformInfoCollector(applicationContext) }
        single<ApplicationVersionProviderApi> { AndroidApplicationVersionProvider(applicationContext) }
        single<LanguageProviderApi> { AndroidLanguageProvider(Locale.getDefault()) }
        single<TransitionSafeCurrentActivityWatchdog> { TransitionSafeCurrentActivityWatchdog().also { it.register() } }
        single<EventsDaoApi> {
            val driver = AndroidSqliteDriver(SapEngagementCloudDB.Schema, applicationContext, DB_NAME)
            AndroidSqlDelightEventsDao(
                db = SapEngagementCloudDB(driver),
                json = get()
            )
        }
        single<SharedPreferences> {
            applicationContext.getSharedPreferences(
                StorageConstants.SUITE_NAME,
                Context.MODE_PRIVATE
            )
        }
        single<PermissionHandlerApi> {
            AndroidPermissionHandler(
                applicationContext,
                get<TransitionSafeCurrentActivityWatchdog>()
            )
        }
        single<StringStorageApi> { StringStorage(sharedPreferences = get()) }
        single<AndroidNotificationSettingsCollectorApi> {
            AndroidNotificationSettingsCollector(
                applicationContext
            )
        }
        single<AndroidConfigApi> {
            AndroidConfig(
                configApi = get(),
                androidNotificationSettingsCollector = get()
            )
        }
        single<AndroidSetupApi> { AndroidSetup(setup = get()) }
        single<PageLocationProviderApi> { PageLocationProvider() }
        single<PlatformCategoryProviderApi> { PlatformCategoryProvider() }
        single<DeviceInfoCollectorApi> {
            val isGoogleAvailable: Boolean = get(named(AvailableServices.Google))
            val isHuaweiAvailable: Boolean = get(named(AvailableServices.Huawei))
            DeviceInfoCollector(
                timezoneProvider = get(),
                languageProvider = get(),
                applicationVersionProvider = get(),
                isGooglePlayServicesAvailable = if (isGoogleAvailable == isHuaweiAvailable) {
                    true
                } else {
                    !isHuaweiAvailable
                },
                clientIdProvider = ClientIdProvider(uuidProvider = get(), storage = get()),
                platformInfoCollector = get(),
                wrapperInfoStorage = get(),
                androidNotificationSettingsCollector = get(),
                json = get(),
                stringStorage = get(),
                sdkContext = get(),
                platformCategoryProvider = get()
            )
        }
        single<State>(named(InitStateTypes.LegacySDKMigration)) {
            val sharedPreferences: SharedPreferences =
                applicationContext.getSharedPreferences(
                    EMARSYS_SECURE_SHARED_PREFERENCES_V3_NAME,
                    Context.MODE_PRIVATE
                )
            val legacySharedPreferencesWrapper = LegacySharedPreferencesWrapper(
                sharedPreferences,
                SharedPreferenceCrypto(
                    sdkLogger = get { parametersOf(SharedPreferenceCrypto::class.simpleName) }
                )
            )
            val legacyMigrationDBOpenHelper = LegacyDBOpenHelper(applicationContext)
            LegacySDKMigrationState(
                legacySharedPreferencesWrapper = legacySharedPreferencesWrapper,
                legacyDBOpenHelper = legacyMigrationDBOpenHelper,
                requestContext = get(),
                stringStorage = get(),
                ioDispatcher = Dispatchers.IO,
                sdkLogger = get { parametersOf(LegacySDKMigrationState::class.simpleName) }
            )
        }
        single<State>(named(StateTypes.PlatformInit)) { PlatformInitState() }
        single<PlatformInitializerApi> {
            PlatformInitializer(
                sdkEventDistributor = get(),
                notificationManager = get(),
                sdkDispatcher = get(named(DispatcherTypes.Sdk))
            )
        }
        single<NotificationManager> { (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) }
        single<NotificationIntentProcessor> {
            NotificationIntentProcessor(
                json = get(),
                actionFactory = get(),
                actionHandler = get(),
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                sdkLogger = get { parametersOf(NotificationIntentProcessor::class.simpleName) }
            )
        }
        single<SilentPushMessageHandler> {
            SilentPushMessageHandler(
                pushActionFactory = get(),
                sdkEventDistributor = get()
            )
        }
        single<NotificationCompatStyler> {
            NotificationCompatStyler(
                downloader = get()
            )
        }
        single<PushMessagePresenter> {
            PushMessagePresenter(
                applicationContext,
                json = get(),
                notificationManager = get(),
                metadataReader = get(),
                notificationCompatStyler = get(),
                platformInfoCollector = get(),
                androidNotificationSettingsCollector = get(),
                sdkLogger = get { parametersOf(PushMessagePresenter::class.simpleName) },
            )
        }
        single<AndroidPushV2Mapper> {
            AndroidPushV2Mapper(
                json = get(),
                logger = get { parametersOf(AndroidPushV2Mapper::class.simpleName) },
                uuidProvider = get()
            )
        }
        single<SilentAndroidPushV2Mapper> {
            SilentAndroidPushV2Mapper(
                json = get(),
                logger = get { parametersOf(SilentAndroidPushV2Mapper::class.simpleName) }
            )
        }
        single<HuaweiPushV2Mapper> {
            HuaweiPushV2Mapper(
                uuidProvider = get(),
                logger = get { parametersOf(HuaweiPushV2Mapper::class.simpleName) },
                json = get()
            )
        }
        single<SilentHuaweiPushV2Mapper> {
            SilentHuaweiPushV2Mapper(
                uuidProvider = get(),
                logger = get { parametersOf(SilentHuaweiPushV2Mapper::class.simpleName) },
                json = get()
            )
        }
        single<AndroidPushMessageFactory> {
            AndroidPushMessageFactory(
                androidPushV2Mapper = get(),
                silentAndroidPushV2Mapper = get(),
                huaweiPushV2Mapper = get(),
                silentHuaweiPushV2Mapper = get()
            )
        }
        single<SdkConfigStoreApi<AndroidEngagementCloudSDKConfig>> {
            AndroidSdkConfigStore(
                typedStorage = get()
            )
        }
        single<ExternalUrlOpenerApi> {
            AndroidExternalUrlOpener(
                applicationContext = applicationContext,
                sdkLogger = get { parametersOf(AndroidExternalUrlOpener::class.simpleName) }
            )
        }
        single<ConnectionWatchDog> {
            val connectivityManager =
                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            AndroidConnectionWatchDog(
                connectivityManager = connectivityManager,
                logger = get { parametersOf(ConnectionWatchDog::class.simpleName) }
            )
        }
        single<LifecycleWatchDog> {
            AndroidLifecycleWatchDog(
                ProcessLifecycleOwner.get().lifecycle,
                ProcessLifecycleOwner.get().lifecycleScope,
                lifecycleWatchDogScope = CoroutineScope(Dispatchers.Default)
            )
        }
        single<FileCacheApi> { AndroidFileCache(applicationContext, FileSystem.SYSTEM) }
        single<InAppViewProviderApi> {
            val inAppJsBridgeFactory = InAppJsBridgeFactory(
                actionFactory = get<EventActionFactoryApi>(),
                json = get(),
                applicationScope = get(named(CoroutineScopeTypes.Application))
            )
            InAppViewProvider(
                applicationContext,
                inAppJsBridgeFactory,
                mainDispatcher = get(named(DispatcherTypes.Main)),
                WebViewProvider(applicationContext, get(named(DispatcherTypes.Main))),
                timestampProvider = get(),
                contentReplacer = get(),
            )
        }
        single<InAppDialogProviderApi> { InAppDialogProvider() }
        single<InAppPresenterApi> {
            InAppPresenter(
                inAppDialogProvider = get(),
                currentActivityWatchdog = get(),
                mainDispatcher = get(named(DispatcherTypes.Main)),
                sdkEventDistributor = get(),
                timestampProvider = get(),
                logger = get { parametersOf(InAppPresenter::class.simpleName) },
                applicationScope = get(named(CoroutineScopeTypes.Application))
            )
        }
        single<InlineInAppViewRendererApi> { AndroidInlineInAppViewRenderer() }
        single<ClipboardHandlerApi> {
            val clipboardManager =
                applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            AndroidClipboardHandler(clipboardManager)
        }
        single<LaunchApplicationHandlerApi> {
            LaunchApplicationHandler(
                applicationContext,
                activityFinder = get<TransitionSafeCurrentActivityWatchdog>(),
                sdkContext = get(),
                sdkLogger = get { parametersOf(LaunchApplicationHandler::class.simpleName) }
            )
        }
        single<LanguageTagValidatorApi> { AndroidLanguageTagValidator() }
        single<SdkConfigStoreApi<AndroidEngagementCloudSDKConfig>>(named(SdkConfigStoreTypes.Android)) {
            AndroidSdkConfigStore(typedStorage = get())
        }
        single<PushInstance>(named(InstanceType.Logging)) {
            LoggingPush(
                storage = get(),
                logger = get { parametersOf(LoggingPush::class.simpleName) }
            )
        }
        single<PushInstance>(named(InstanceType.Gatherer)) {
            PushGatherer(
                context = get(),
                storage = get(),
                sdkContext = get()
            )
        }
        single<PushInstance>(named(InstanceType.Internal)) {
            PushInternal(
                storage = get(),
                pushContext = get(),
                sdkEventDistributor = get(),
                sdkContext = get(),
                sdkLogger = get { parametersOf(PushInternal::class.simpleName) }
            )
        }
        single<PushApi> {
            Push(
                loggingApi = get(named(InstanceType.Logging)),
                gathererApi = get(named(InstanceType.Gatherer)),
                internalApi = get(named(InstanceType.Internal)),
                sdkContext = get()
            )
        }
        single<AndroidDeepLinkApi> {
            AndroidDeepLink(
                sdkDispatcher = get(named(DispatcherTypes.Sdk)),
                deepLink = get<DeepLinkApi>(),
                sdkLogger = get {
                    parametersOf(AndroidDeepLink::class.simpleName)
                }
            )
        }
        single<Boolean>(named(AvailableServices.Google)) {
            GoogleApiAvailabilityLight.getInstance()
                .isGooglePlayServicesAvailable(applicationContext) == ConnectionResult.SUCCESS
        }
        single<Boolean>(named(AvailableServices.Huawei)) {
            try {
                val huaweiServiceCheckerClass =
                    Class.forName(
                        "com.sap.ec.HuaweiServiceChecker",
                        true,
                        applicationContext.classLoader
                    )
                val huaweiServiceChecker =
                    huaweiServiceCheckerClass.getDeclaredConstructor().newInstance()

                val types = listOf<Class<*>>(Context::class.java).toTypedArray()
                val method: Method = huaweiServiceCheckerClass.getDeclaredMethod("check", *types)
                method.isAccessible = true

                method.invoke(huaweiServiceChecker, applicationContext) as Boolean
            } catch (ignored: Exception) {
                false
            }
        }
    }
}

enum class AvailableServices {
    Google, Huawei
}

actual fun SdkKoinIsolationContext.loadPlatformModules(): List<Module> {
    return listOf(AndroidInjection.androidModules)
}