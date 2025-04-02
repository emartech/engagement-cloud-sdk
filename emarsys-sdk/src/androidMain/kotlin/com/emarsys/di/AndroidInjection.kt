package com.emarsys.di

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.emarsys.AndroidEmarsysConfig
import com.emarsys.applicationContext
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
import com.emarsys.mobileengage.permission.AndroidPermissionHandler
import com.emarsys.mobileengage.push.AndroidPushMessageFactory
import com.emarsys.mobileengage.push.NotificationCompatStyler
import com.emarsys.mobileengage.push.NotificationIntentProcessor
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.SilentPushMessageHandler
import com.emarsys.mobileengage.push.mapper.AndroidPushV1Mapper
import com.emarsys.mobileengage.push.mapper.AndroidPushV2Mapper
import com.emarsys.mobileengage.push.mapper.SilentAndroidPushV1Mapper
import com.emarsys.mobileengage.push.mapper.SilentAndroidPushV2Mapper
import com.emarsys.mobileengage.pushtoinapp.PushToInAppHandler
import com.emarsys.mobileengage.url.AndroidExternalUrlOpener
import com.emarsys.setup.PlatformInitState
import com.emarsys.setup.PlatformInitializer
import com.emarsys.setup.PlatformInitializerApi
import com.emarsys.setup.config.AndroidSdkConfigStore
import com.emarsys.setup.config.SdkConfigStoreApi
import com.emarsys.sqldelight.EmarsysDB
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import com.emarsys.watchdog.connection.AndroidConnectionWatchDog
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.AndroidLifecycleWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okio.FileSystem
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
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
            DeviceInfoCollector(
                timezoneProvider = get(),
                languageProvider = get(),
                applicationVersionProvider = get(),
                isGooglePlayServicesAvailable = true,
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
                sdkEventFlow = get(named(EventFlowTypes.InternalEventFlow)),
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
                sdkLogger = get { parametersOf(NotificationIntentProcessor::class.simpleName) }
            )
        }
        single<SilentPushMessageHandler> {
            SilentPushMessageHandler(
                pushActionFactory = get(),
                sdkEventFlow = get(named(EventFlowTypes.InternalEventFlow))
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
                inAppDownloader = get(),
                sdkLogger = get { parametersOf(PushMessagePresenter::class.simpleName) }
            )
        }
        single<AndroidPushV1Mapper> {
            AndroidPushV1Mapper(
                json = get(),
                uuidProvider = get(),
                logger = get { parametersOf(AndroidPushV1Mapper::class.simpleName) }
            )
        }
        single<AndroidPushV2Mapper> {
            AndroidPushV2Mapper(
                json = get(),
                logger = get { parametersOf(AndroidPushV2Mapper::class.simpleName) },
                uuidProvider = get()
            )
        }
        single<SilentAndroidPushV1Mapper> {
            SilentAndroidPushV1Mapper(
                json = get(),
                logger = get { parametersOf(SilentAndroidPushV1Mapper::class.simpleName) }
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
                androidPushV1Mapper = get(),
                silentAndroidPushV1Mapper = get(),
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
    }
}

actual fun SdkKoinIsolationContext.loadPlatformModules(): List<Module> {
    return listOf(AndroidInjection.androidModules)
}