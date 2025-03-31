package com.emarsys.di

import com.emarsys.SdkConfig
import com.emarsys.api.push.LoggingPush
import com.emarsys.api.push.Push
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushGatherer
import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.PushInternal
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.badge.WebBadgeCountHandler
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.cache.WebFileCache
import com.emarsys.core.clipboard.WebClipboardHandler
import com.emarsys.core.db.EmarsysIndexedDb
import com.emarsys.core.db.EmarsysIndexedDbObjectStore
import com.emarsys.core.db.EmarsysObjectStoreConfig
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.db.events.JSEventsDao
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.WebPlatformInfoCollector
import com.emarsys.core.language.LanguageTagValidator
import com.emarsys.core.language.LanguageTagValidatorApi
import com.emarsys.core.launchapplication.JsLaunchApplicationHandler
import com.emarsys.core.log.Logger
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.permission.WebPermissionHandler
import com.emarsys.core.provider.ApplicationVersionProvider
import com.emarsys.core.provider.WebLanguageProvider
import com.emarsys.core.providers.ApplicationVersionProviderApi
import com.emarsys.core.providers.ClientIdProvider
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.LanguageProviderApi
import com.emarsys.core.providers.TimezoneProviderApi
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.url.WebExternalUrlOpener
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import com.emarsys.mobileengage.inapp.InAppJsBridgeFactory
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppScriptExtractor
import com.emarsys.mobileengage.inapp.InAppScriptExtractorApi
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.WebInAppPresenter
import com.emarsys.mobileengage.inapp.WebInAppViewProvider
import com.emarsys.mobileengage.push.PushNotificationClickHandler
import com.emarsys.mobileengage.push.PushService
import com.emarsys.mobileengage.push.PushServiceContext
import com.emarsys.mobileengage.pushtoinapp.WebPushToInAppHandler
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.setup.PlatformInitState
import com.emarsys.setup.PlatformInitializer
import com.emarsys.setup.PlatformInitializerApi
import com.emarsys.setup.config.JsEmarsysConfigStore
import com.emarsys.setup.config.SdkConfigStoreApi
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.connection.WebConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import com.emarsys.watchdog.lifecycle.WebLifeCycleWatchDog
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import web.broadcast.BroadcastChannel
import web.dom.document
import web.idb.indexedDB

internal actual class PlatformDependencyCreator actual constructor(
    private val sdkContext: SdkContextApi,
    private val uuidProvider: UuidProviderApi,
    private val sdkLogger: Logger,
    private val json: Json,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    actionHandler: ActionHandlerApi,
    timestampProvider: InstantProvider
) : DependencyCreator {
    private val emarsysIndexedDb = EmarsysIndexedDb(indexedDB, sdkLogger)

    private val stringStorage: StringStorageApi by lazy {
        StringStorage(window.localStorage)
    }

    actual override fun createPlatformInitializer(
        pushActionFactory: PushActionFactoryApi,
        pushActionHandler: ActionHandlerApi
    ): PlatformInitializerApi {
        val pushNotificationClickHandler = PushNotificationClickHandler(
            pushActionFactory,
            pushActionHandler,
            BroadcastChannel(WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME),
            CoroutineScope(Dispatchers.Default + SupervisorJob()),
            sdkLogger
        )
        val webBadgeCountHandler = WebBadgeCountHandler(
            BroadcastChannel(WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED),
            sdkEventFlow,
            CoroutineScope(Dispatchers.Default + SupervisorJob()),
            sdkLogger
        )
        return PlatformInitializer(pushNotificationClickHandler, webBadgeCountHandler)

    }

    actual override fun createStringStorage(): StringStorageApi {
        return stringStorage
    }

    actual override fun createEventsDao(): EventsDaoApi {
        val emarsysIndexedDbObjectStore = EmarsysIndexedDbObjectStore(
            emarsysIndexedDb,
            EmarsysObjectStoreConfig.Events,
            json,
            sdkLogger,
            sdkContext.sdkDispatcher
        )
        return JSEventsDao(emarsysIndexedDbObjectStore, sdkLogger)
    }

    actual override fun createDeviceInfoCollector(
        timezoneProvider: TimezoneProviderApi,
        typedStorage: TypedStorageApi
    ): DeviceInfoCollector {
        return DeviceInfoCollector(
            ClientIdProvider(uuidProvider, stringStorage),
            timezoneProvider,
            createWebDeviceInfoCollector(),
            createApplicationVersionProvider(),
            createLanguageProvider(),
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
        return PlatformInitState(
            PushService(pushServiceContext, storage),
            sdkContext
        )
    }

    actual override fun createPermissionHandler(): PermissionHandlerApi {
        return WebPermissionHandler()
    }

    actual override fun createExternalUrlOpener(): ExternalUrlOpenerApi {
        return WebExternalUrlOpener(window, sdkLogger)
    }

    actual override fun createPushToInAppHandler(
        inAppDownloader: InAppDownloaderApi,
        inAppHandler: InAppHandlerApi
    ): PushToInAppHandlerApi {
        return WebPushToInAppHandler(inAppDownloader, inAppHandler)
    }

    actual override fun createConnectionWatchDog(sdkLogger: Logger): ConnectionWatchDog {
        return WebConnectionWatchDog(window)
    }

    actual override fun createLifeCycleWatchDog(): LifecycleWatchDog {
        return WebLifeCycleWatchDog(document, CoroutineScope(Dispatchers.Default))
    }

    private val pushServiceContext: PushServiceContext by lazy {
        PushServiceContext()
    }

    private fun createWebDeviceInfoCollector(): WebPlatformInfoCollector {
        return WebPlatformInfoCollector(getNavigatorData())
    }

    actual override fun createApplicationVersionProvider(): ApplicationVersionProviderApi {
        return ApplicationVersionProvider()
    }

    actual override fun createLanguageProvider(): LanguageProviderApi {
        return WebLanguageProvider()
    }

    actual override fun createFileCache(): FileCacheApi {
        return WebFileCache()
    }

    private val inappScriptExtractor: InAppScriptExtractorApi by lazy {
        InAppScriptExtractor()
    }

    actual override fun createInAppViewProvider(eventActionFactory: EventActionFactoryApi): InAppViewProviderApi {
        return WebInAppViewProvider(
            inappScriptExtractor,
            InAppJsBridgeFactory(eventActionFactory, json, Dispatchers.Main)
        )
    }

    actual override fun createInAppPresenter(): InAppPresenterApi {
        return WebInAppPresenter(sdkEventFlow, sdkContext.sdkDispatcher)
    }

    actual override fun createClipboardHandler(): ClipboardHandlerApi {
        return WebClipboardHandler(window.navigator.clipboard)
    }

    actual override fun createLaunchApplicationHandler(): LaunchApplicationHandlerApi {
        return JsLaunchApplicationHandler()
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
        return PushInternal(pushClient, storage, pushContext)
    }

    actual override fun createPushApi(
        pushInternal: PushInstance,
        storage: StringStorageApi,
        pushContext: PushContextApi,
    ): PushApi {
        val loggingPush = LoggingPush(storage, sdkLogger)
        val pushGatherer = PushGatherer(pushContext, storage)
        return Push(loggingPush, pushGatherer, pushInternal, sdkContext)
    }

    actual override fun createSdkConfigStore(typedStorage: TypedStorageApi): SdkConfigStoreApi<SdkConfig> {
        return JsEmarsysConfigStore(typedStorage)
    }

    private fun getNavigatorData(): String {
        return listOf(
            window.navigator.platform,
            window.navigator.userAgent,
            window.navigator.appVersion,
            window.navigator.vendor,
        ).joinToString(" ")
    }
}