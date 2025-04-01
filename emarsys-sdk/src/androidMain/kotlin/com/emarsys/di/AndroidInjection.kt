package com.emarsys.di

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import com.emarsys.AndroidEmarsysConfig
import com.emarsys.applicationContext
import com.emarsys.core.device.PlatformInfoCollector
import com.emarsys.core.device.PlatformInfoCollectorApi
import com.emarsys.core.resource.MetadataReader
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.mobileengage.push.AndroidPushMessageFactory
import com.emarsys.mobileengage.push.NotificationCompatStyler
import com.emarsys.mobileengage.push.NotificationIntentProcessor
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.SilentPushMessageHandler
import com.emarsys.mobileengage.push.mapper.AndroidPushV1Mapper
import com.emarsys.mobileengage.push.mapper.AndroidPushV2Mapper
import com.emarsys.mobileengage.push.mapper.SilentAndroidPushV1Mapper
import com.emarsys.mobileengage.push.mapper.SilentAndroidPushV2Mapper
import com.emarsys.setup.PlatformInitializer
import com.emarsys.setup.PlatformInitializerApi
import com.emarsys.setup.config.AndroidSdkConfigStore
import com.emarsys.setup.config.SdkConfigStoreApi
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object AndroidInjection {
    val androidModules = module {
        single<MetadataReader> { MetadataReader(applicationContext) }
        single<PlatformInfoCollectorApi> { PlatformInfoCollector(applicationContext) }
        single<TransitionSafeCurrentActivityWatchdog> { TransitionSafeCurrentActivityWatchdog().also { it.register() } }
        single<SharedPreferences> {
            applicationContext.getSharedPreferences(
                StorageConstants.SUITE_NAME,
                Context.MODE_PRIVATE
            )
        }
        single<StringStorageApi> { StringStorage( sharedPreferences = get()) }
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
    }
}

actual fun SdkKoinIsolationContext.loadPlatformModules(): List<Module> {
    return listOf(AndroidInjection.androidModules)
}