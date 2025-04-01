package com.emarsys.di

import com.emarsys.SdkConfig
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushInstance
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.language.LanguageTagValidatorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.providers.ApplicationVersionProviderApi
import com.emarsys.core.providers.LanguageProviderApi
import com.emarsys.core.providers.TimezoneProviderApi
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.setup.config.SdkConfigStoreApi
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json

internal interface DependencyCreator {

    fun createEventsDao(): EventsDaoApi

    fun createDeviceInfoCollector(
        timezoneProvider: TimezoneProviderApi,
        typedStorage: TypedStorageApi
    ): DeviceInfoCollector

    fun createPlatformInitState(
        pushApi: PushApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContextApi,
        actionFactory: EventActionFactoryApi,
        storage: StringStorageApi
    ): State

    fun createPermissionHandler(): PermissionHandlerApi

    fun createExternalUrlOpener(): ExternalUrlOpenerApi

    fun createPushToInAppHandler(
        inAppDownloader: InAppDownloaderApi,
        inAppHandler: InAppHandlerApi
    ): PushToInAppHandlerApi

    fun createConnectionWatchDog(sdkLogger: Logger): ConnectionWatchDog

    fun createLifeCycleWatchDog(): LifecycleWatchDog

    fun createApplicationVersionProvider(): ApplicationVersionProviderApi

    fun createLanguageProvider(): LanguageProviderApi

    fun createFileCache(): FileCacheApi

    fun createInAppViewProvider(eventActionFactory: EventActionFactoryApi): InAppViewProviderApi

    fun createInAppPresenter(): InAppPresenterApi

    fun createClipboardHandler(): ClipboardHandlerApi

    fun createLaunchApplicationHandler(): LaunchApplicationHandlerApi

    fun createLanguageTagValidator(): LanguageTagValidatorApi

    fun createPushInternal(
        pushClient: PushClientApi,
        storage: StringStorageApi,
        pushContext: PushContextApi,
        eventClient: EventClientApi,
        pushActionFactory: PushActionFactoryApi,
        json: Json,
        sdkDispatcher: CoroutineDispatcher
    ): PushInstance

    fun createPushApi(
        pushInternal: PushInstance,
        storage: StringStorageApi,
        pushContext: PushContextApi
    ): PushApi

    fun createSdkConfigStore(typedStorage: TypedStorageApi): SdkConfigStoreApi<SdkConfig>
}