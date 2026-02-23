package com.sap.ec.di

import JsEngagementCloudSDKConfig
import com.sap.ec.api.config.JSConfig
import com.sap.ec.api.config.JSConfigApi
import com.sap.ec.api.contact.JSContact
import com.sap.ec.api.contact.JSContactApi
import com.sap.ec.api.deeplink.JSDeepLink
import com.sap.ec.api.deeplink.JSDeepLinkApi
import com.sap.ec.api.embeddedmessaging.JsEmbeddedMessaging
import com.sap.ec.api.embeddedmessaging.JsEmbeddedMessagingApi
import com.sap.ec.api.events.EventEmitter
import com.sap.ec.api.events.EventEmitterApi
import com.sap.ec.api.inapp.JSInApp
import com.sap.ec.api.inapp.JSInAppApi
import com.sap.ec.api.push.JSPush
import com.sap.ec.api.push.JSPushApi
import com.sap.ec.api.push.LoggingPush
import com.sap.ec.api.push.Push
import com.sap.ec.api.push.PushApi
import com.sap.ec.api.push.PushConstants.WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
import com.sap.ec.api.push.PushConstants.WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME
import com.sap.ec.api.push.PushGatherer
import com.sap.ec.api.push.PushInstance
import com.sap.ec.api.push.PushInternal
import com.sap.ec.api.setup.JsSetup
import com.sap.ec.api.setup.JsSetupApi
import com.sap.ec.api.tracking.JSTracking
import com.sap.ec.api.tracking.JSTrackingApi
import com.sap.ec.core.actions.clipboard.ClipboardHandlerApi
import com.sap.ec.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.sap.ec.core.badge.WebBadgeCountHandler
import com.sap.ec.core.badge.WebBadgeCountHandlerApi
import com.sap.ec.core.cache.FileCacheApi
import com.sap.ec.core.cache.WebFileCache
import com.sap.ec.core.clipboard.WebClipboardHandler
import com.sap.ec.core.db.ECIndexedDbObjectStore
import com.sap.ec.core.db.EngagementCloudIndexedDb
import com.sap.ec.core.db.EngagementCloudObjectStoreConfig
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.db.events.JSEventsDao
import com.sap.ec.core.device.DeviceInfoCollector
import com.sap.ec.core.device.DeviceInfoCollectorApi
import com.sap.ec.core.device.WebPlatformInfoCollector
import com.sap.ec.core.device.notification.WebNotificationSettingsCollector
import com.sap.ec.core.device.notification.WebNotificationSettingsCollectorApi
import com.sap.ec.core.language.LanguageTagValidatorApi
import com.sap.ec.core.language.WebLanguageTagValidator
import com.sap.ec.core.launchapplication.JsLaunchApplicationHandler
import com.sap.ec.core.permission.PermissionHandlerApi
import com.sap.ec.core.permission.WebPermissionHandler
import com.sap.ec.core.providers.ApplicationVersionProviderApi
import com.sap.ec.core.providers.ClientIdProvider
import com.sap.ec.core.providers.LanguageProviderApi
import com.sap.ec.core.providers.WebApplicationVersionProvider
import com.sap.ec.core.providers.WebLanguageProvider
import com.sap.ec.core.providers.pagelocation.PageLocationProvider
import com.sap.ec.core.providers.pagelocation.PageLocationProviderApi
import com.sap.ec.core.providers.platform.PlatformCategoryProvider
import com.sap.ec.core.providers.platform.PlatformCategoryProviderApi
import com.sap.ec.core.state.State
import com.sap.ec.core.storage.StringStorage
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.core.url.ExternalUrlOpenerApi
import com.sap.ec.core.url.WebExternalUrlOpener
import com.sap.ec.enable.PlatformInitState
import com.sap.ec.enable.PlatformInitializer
import com.sap.ec.enable.PlatformInitializerApi
import com.sap.ec.enable.config.JsECConfigStore
import com.sap.ec.enable.config.SdkConfigStoreApi
import com.sap.ec.init.states.LegacySDKMigrationState
import com.sap.ec.mobileengage.inapp.WebInAppPresenter
import com.sap.ec.mobileengage.inapp.WebInAppViewProvider
import com.sap.ec.mobileengage.inapp.WebInlineInAppViewRenderer
import com.sap.ec.mobileengage.inapp.iframe.IframeContainerResizer
import com.sap.ec.mobileengage.inapp.iframe.IframeContainerResizerApi
import com.sap.ec.mobileengage.inapp.iframe.IframeFactory
import com.sap.ec.mobileengage.inapp.iframe.IframeFactoryApi
import com.sap.ec.mobileengage.inapp.iframe.MessageChannelProvider
import com.sap.ec.mobileengage.inapp.iframe.MessageChannelProviderApi
import com.sap.ec.mobileengage.inapp.presentation.InAppPresenterApi
import com.sap.ec.mobileengage.inapp.presentation.InlineInAppViewRendererApi
import com.sap.ec.mobileengage.inapp.view.InAppViewProviderApi
import com.sap.ec.mobileengage.push.PushNotificationClickHandler
import com.sap.ec.mobileengage.push.PushNotificationClickHandlerApi
import com.sap.ec.mobileengage.push.PushService
import com.sap.ec.mobileengage.push.PushServiceContext
import com.sap.ec.mobileengage.push.PushServiceContextApi
import com.sap.ec.watchdog.connection.ConnectionWatchDog
import com.sap.ec.watchdog.connection.WebConnectionWatchDog
import com.sap.ec.watchdog.lifecycle.LifecycleWatchDog
import com.sap.ec.watchdog.lifecycle.WebLifeCycleWatchDog
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import web.broadcast.BroadcastChannel
import web.dom.document
import web.idb.indexedDB

object WebInjection {
    val webModules = module {
        single<StringStorageApi> { StringStorage(window.localStorage) }
        single<SdkConfigStoreApi<JsEngagementCloudSDKConfig>> {
            JsECConfigStore(
                typedStorage = get()
            )
        }
        single<LanguageProviderApi> { WebLanguageProvider() }
        single<ApplicationVersionProviderApi> { WebApplicationVersionProvider() }
        single<PageLocationProviderApi> { PageLocationProvider() }
        single<EventsDaoApi> {
            val engagementCloudIndexedDb = EngagementCloudIndexedDb(
                indexedDBFactory = indexedDB,
                sdkLogger = get { parametersOf(EngagementCloudIndexedDb::class.simpleName) })
            val ECIndexedDbObjectStore = ECIndexedDbObjectStore(
                engagementCloudIndexedDb,
                EngagementCloudObjectStoreConfig.Events,
                json = get<Json>(),
                logger = get { parametersOf(ECIndexedDbObjectStore::class.simpleName) },
                get(named(DispatcherTypes.Sdk))
            )
            JSEventsDao(
                ecIndexedDbObjectStore = ECIndexedDbObjectStore,
                logger = get { parametersOf(JSEventsDao::class.simpleName) }
            )
        }
        single<PlatformCategoryProviderApi> { PlatformCategoryProvider() }
        single<PermissionHandlerApi> { WebPermissionHandler() }
        single<DeviceInfoCollectorApi> {
            DeviceInfoCollector(
                clientIdProvider = ClientIdProvider(uuidProvider = get(), storage = get()),
                timezoneProvider = get(),
                webPlatformInfoCollector = WebPlatformInfoCollector(getNavigatorData()),
                applicationVersionProvider = get(),
                languageProvider = get(),
                wrapperInfoStorage = get(),
                webNotificationSettingsCollector = get(),
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
                sdkLogger = get { parametersOf(LegacySDKMigrationState::class.simpleName) }
            )
        }
        single<PushServiceContextApi> { PushServiceContext() }
        single<State>(named(StateTypes.PlatformInit)) {
            val pushService = PushService(get(), storage = get<StringStorageApi>())
            PlatformInitState(
                pushService = pushService,
                sdkContext = get()
            )
        }
        single<PushNotificationClickHandlerApi> {
            PushNotificationClickHandler(
                actionFactory = get(),
                actionHandler = get(),
                onNotificationClickedBroadcastChannel = BroadcastChannel(
                    WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME
                ),
                coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
                sdkLogger = get { parametersOf(PushNotificationClickHandler::class.simpleName) }
            )
        }
        single<WebBadgeCountHandlerApi> {
            WebBadgeCountHandler(
                onBadgeCountUpdateReceivedBroadcastChannel = BroadcastChannel(
                    WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
                ),
                sdkEventDistributor = get(),
                coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
                sdkLogger = get { parametersOf(WebBadgeCountHandler::class.simpleName) }

            )
        }
        single<EventEmitterApi> {
            EventEmitter(
                sdkPublicEventFLow = get(named(EventFlowTypes.Public)),
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                listeners = mutableMapOf(),
                onceListeners = mutableMapOf(),
                uuidProvider = get(),
                json = get(),
                logger = get { parametersOf(EventEmitter::class.simpleName) }
            )
        }
        single<PlatformInitializerApi> {
            PlatformInitializer(
                pushNotificationClickHandler = get(),
                webBadgeCountHandler = get()
            )
        }
        single<ExternalUrlOpenerApi> {
            WebExternalUrlOpener(
                window = window,
                sdkLogger = get { parametersOf(WebExternalUrlOpener::class.simpleName) })
        }
        single<ConnectionWatchDog> { WebConnectionWatchDog(window) }
        single<LifecycleWatchDog> {
            WebLifeCycleWatchDog(
                document = document,
                CoroutineScope(Dispatchers.Default)
            )
        }
        single<FileCacheApi> { WebFileCache() }
        single<IframeContainerResizerApi> { IframeContainerResizer() }
        single<MessageChannelProviderApi> {
            MessageChannelProvider(
                eventActionFactory = get(),
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                iframeContainerResizer = get(),
                logger = get { parametersOf(MessageChannelProvider::class.simpleName) },
                json = get()
            )
        }
        single<InAppViewProviderApi> {
            WebInAppViewProvider(
                timestampProvider = get(),
                contentReplacer = get(),
                iframeFactory = get(),
                messageChannelProvider = get()
            )
        }
        single<InAppPresenterApi> {
            WebInAppPresenter(
                sdkEventDistributor = get(),
                sdkDispatcher = get(named(DispatcherTypes.Sdk)),
                logger = get { parametersOf(WebInAppPresenter::class.simpleName) }
            )
        }
        single<IframeFactoryApi> { IframeFactory() }
        single<ClipboardHandlerApi> { WebClipboardHandler(window.navigator.clipboard) }
        single<LaunchApplicationHandlerApi> { JsLaunchApplicationHandler() }
        single<InlineInAppViewRendererApi> { WebInlineInAppViewRenderer() }
        single<LanguageTagValidatorApi> { WebLanguageTagValidator() }
        single<SdkConfigStoreApi<JsEngagementCloudSDKConfig>>(named(SdkConfigStoreTypes.Web)) {
            JsECConfigStore(
                typedStorage = get()
            )
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
        single<JsSetupApi> { JsSetup(get()) }
        single<WebNotificationSettingsCollectorApi> {
            WebNotificationSettingsCollector(pushServiceContext = get())
        }
        single<JSConfigApi> {
            JSConfig(
                configApi = get(),
                webNotificationSettingsCollector = get()
            )
        }
        single<JSContactApi> { JSContact(contactApi = get()) }
        single<JSTrackingApi> { JSTracking(trackingApi = get()) }
        single<JSPushApi> { JSPush(pushApi = get()) }
        single<JSDeepLinkApi> { JSDeepLink(deepLinkApi = get()) }
        single<JSInAppApi> { JSInApp(inAppApi = get()) }
        single<JsEmbeddedMessagingApi> { JsEmbeddedMessaging(embeddedMessaging = get()) }
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

actual fun SdkKoinIsolationContext.loadPlatformModules(): List<Module> {
    return listOf(WebInjection.webModules)
}