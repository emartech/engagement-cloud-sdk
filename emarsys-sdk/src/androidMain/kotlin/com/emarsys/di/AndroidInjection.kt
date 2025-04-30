package com.emarsys.di

import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.emarsys.AndroidEmarsysConfig
import com.emarsys.api.deeplink.AndroidDeepLink
import com.emarsys.api.deeplink.AndroidDeepLinkApi
import com.emarsys.api.deeplink.DeepLinkApi
import com.emarsys.api.push.LoggingPush
import com.emarsys.api.push.Push
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushGatherer
import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.PushInternal
import com.emarsys.applicationContext
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.cache.AndroidFileCache
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.db.events.AndroidSqlDelightEventsDao
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.device.AndroidLanguageProvider
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.device.PlatformInfoCollector
import com.emarsys.core.device.PlatformInfoCollectorApi
import com.emarsys.core.language.AndroidLanguageTagValidator
import com.emarsys.core.language.LanguageTagValidatorApi
import com.emarsys.core.launchapplication.LaunchApplicationHandler
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.provider.AndroidApplicationVersionProvider
import com.emarsys.core.providers.ApplicationVersionProviderApi
import com.emarsys.core.providers.ClientIdProvider
import com.emarsys.core.providers.LanguageProviderApi
import com.emarsys.core.resource.MetadataReader
import com.emarsys.core.state.State
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StorageConstants.DB_NAME
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.clipboard.AndroidClipboardHandler
import com.emarsys.mobileengage.inapp.InAppJsBridgeProvider
import com.emarsys.mobileengage.inapp.InAppPresenter
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewProvider
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.WebViewProvider
import com.emarsys.mobileengage.permission.AndroidPermissionHandler
import com.emarsys.mobileengage.push.AndroidPushMessageFactory
import com.emarsys.mobileengage.push.NotificationCompatStyler
import com.emarsys.mobileengage.push.NotificationIntentProcessor
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.SilentPushMessageHandler
import com.emarsys.mobileengage.push.mapper.AndroidPushV2Mapper
import com.emarsys.mobileengage.push.mapper.SilentAndroidPushV2Mapper
import com.emarsys.mobileengage.pushtoinapp.PushToInAppHandler
import com.emarsys.mobileengage.url.AndroidExternalUrlOpener
import com.emarsys.enable.PlatformInitState
import com.emarsys.enable.PlatformInitializer
import com.emarsys.enable.PlatformInitializerApi
import com.emarsys.enable.config.AndroidSdkConfigStore
import com.emarsys.enable.config.SdkConfigStoreApi
import com.emarsys.sqldelight.EmarsysDB
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import com.emarsys.watchdog.connection.AndroidConnectionWatchDog
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.AndroidLifecycleWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
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
            val driver = AndroidSqliteDriver(EmarsysDB.Schema, applicationContext, DB_NAME)
            AndroidSqlDelightEventsDao(
                db = EmarsysDB(driver),
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
                json = get(),
                stringStorage = get(),
                sdkContext = get()
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
                sdkLogger = get { parametersOf(PushMessagePresenter::class.simpleName) }
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
        single<AndroidPushMessageFactory> {
            AndroidPushMessageFactory(
                androidPushV2Mapper = get(),
                silentAndroidPushV2Mapper = get(),
            )
        }
        single<SdkConfigStoreApi<AndroidEmarsysConfig>> {
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
        single<PushToInAppHandlerApi> {
            PushToInAppHandler(
                downloader = get(),
                inAppHandler = get(),
                sdkLogger = get { parametersOf(PushToInAppHandler::class.simpleName) }
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
            val inAppJsBridgeProvider = InAppJsBridgeProvider(
                actionFactory = get<EventActionFactoryApi>(),
                json = get(),
                sdkDispatcher = get(named(DispatcherTypes.Sdk))
            )
            InAppViewProvider(
                applicationContext,
                inAppJsBridgeProvider,
                mainDispatcher = get(named(DispatcherTypes.Main)),
                WebViewProvider(applicationContext, get(named(DispatcherTypes.Main))),
                timestampProvider = get()
            )
        }
        single<InAppPresenterApi> {
            InAppPresenter(
                currentActivityWatchdog = get(),
                mainDispatcher = get(named(DispatcherTypes.Main)),
                sdkEventDistributor = get(),
                timestampProvider = get(),
                logger = get { parametersOf(InAppPresenter::class.simpleName) },
                applicationScope = get(named(CoroutineScopeTypes.Application))
            )
        }
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
        single<SdkConfigStoreApi<AndroidEmarsysConfig>>(named(SdkConfigStoreTypes.Android)) {
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
                storage = get()
            )
        }
        single<PushInstance>(named(InstanceType.Internal)) {
            PushInternal(
                storage = get(),
                pushContext = get(),
                sdkEventDistributor = get(),
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
                        "com.emarsys.HuaweiServiceChecker",
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