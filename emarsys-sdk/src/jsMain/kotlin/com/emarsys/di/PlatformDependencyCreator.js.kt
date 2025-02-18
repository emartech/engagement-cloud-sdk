package com.emarsys.di

import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.LoggingPush
import com.emarsys.api.push.Push
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME
import com.emarsys.api.push.PushGatherer
import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.PushInternal
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.badge.WebBadgeCountHandler
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.cache.WebFileCache
import com.emarsys.core.clipboard.ClipboardHandlerApi
import com.emarsys.core.clipboard.WebClipboardHandler
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.WebPlatformInfoCollector
import com.emarsys.core.launchapplication.JsLaunchApplicationHandler
import com.emarsys.core.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.message.MsgHubApi
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.permission.WebPermissionHandler
import com.emarsys.core.provider.ApplicationVersionProvider
import com.emarsys.core.provider.WebLanguageProvider
import com.emarsys.core.providers.Provider
import com.emarsys.core.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.url.WebExternalUrlOpener
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import com.emarsys.mobileengage.inapp.InAppJsBridge
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
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import web.broadcast.BroadcastChannel
import web.dom.document

actual class PlatformDependencyCreator actual constructor(
    private val sdkContext: SdkContextApi,
    private val uuidProvider: Provider<String>,
    private val sdkLogger: Logger,
    private val json: Json,
    private val msgHub: MsgHubApi,
    actionHandler: ActionHandlerApi,
    sdkEventFlow: MutableSharedFlow<SdkEvent>,
    timestampProvider: Provider<Instant>
) : DependencyCreator {

    actual override fun createPlatformInitializer(
        sdkEventFlow: MutableSharedFlow<SdkEvent>,
        pushActionFactory: ActionFactoryApi<ActionModel>,
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

    actual override fun createPlatformContext(pushActionFactory: ActionFactoryApi<ActionModel>): PlatformContext {
        return JSPlatformContext()
    }

    actual override fun createStorage(): TypedStorageApi<String?> {
        return StringStorage(window.localStorage)
    }

    actual override fun createDeviceInfoCollector(
        timezoneProvider: Provider<String>,
        storage: TypedStorageApi<String?>
    ): DeviceInfoCollector {
        return DeviceInfoCollector(
            uuidProvider,
            timezoneProvider,
            createWebDeviceInfoCollector(),
            storage,
            createApplicationVersionProvider(), createLanguageProvider(), json,
        )
    }

    actual override fun createPlatformInitState(
        pushApi: PushApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>,
        downloaderApi: DownloaderApi,
        inAppDownloader: InAppDownloaderApi,
        storage: TypedStorageApi<String?>,
        sdkEventFlow: MutableSharedFlow<SdkEvent>
    ): State {
        val scope = CoroutineScope(sdkDispatcher)
        val inappJsBridge = InAppJsBridge(actionFactory, json, scope)

        return PlatformInitState(
            inappJsBridge,
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

    actual override fun createConnectionWatchDog(sdkLogger: SdkLogger): ConnectionWatchDog {
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

    actual override fun createApplicationVersionProvider(): Provider<String> {
        return ApplicationVersionProvider()
    }

    actual override fun createLanguageProvider(): Provider<String> {
        return WebLanguageProvider()
    }

    actual override fun createFileCache(): FileCacheApi {
        return WebFileCache()
    }

    private val inappScriptExtractor: InAppScriptExtractorApi by lazy {
        InAppScriptExtractor()
    }

    actual override fun createInAppViewProvider(actionFactory: ActionFactoryApi<ActionModel>): InAppViewProviderApi {
        return WebInAppViewProvider(inappScriptExtractor)
    }

    actual override fun createInAppPresenter(): InAppPresenterApi {
        return WebInAppPresenter(msgHub)
    }

    actual override fun createClipboardHandler(): ClipboardHandlerApi {
        return WebClipboardHandler(window.navigator.clipboard)
    }

    actual override fun createLaunchApplicationHandler(): LaunchApplicationHandlerApi {
        return JsLaunchApplicationHandler()
    }

    actual override fun createPushInternal(
        pushClient: PushClientApi,
        storage: TypedStorageApi<String?>,
        pushContext: ApiContext<PushCall>,
        eventClient: EventClientApi,
        actionFactory: ActionFactoryApi<ActionModel>,
        json: Json,
        sdkDispatcher: CoroutineDispatcher,
        sdkEventFlow: MutableSharedFlow<SdkEvent>
    ): PushInstance {
        return PushInternal(pushClient, storage, pushContext, sdkLogger)
    }

    actual override fun createPushApi(
        pushInternal: PushInstance,
        storage: TypedStorageApi<String?>,
        pushContext: ApiContext<PushCall>,
    ): PushApi {
        val loggingPush = LoggingPush(sdkLogger, storage)
        val pushGatherer = PushGatherer(pushContext, storage)
        return Push(loggingPush, pushGatherer, pushInternal, sdkContext)
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