package com.emarsys.di

import com.emarsys.JsEmarsysConfig
import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME
import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.PushInternal
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.badge.WebBadgeCountHandler
import com.emarsys.core.badge.WebBadgeCountHandlerApi
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.cache.WebFileCache
import com.emarsys.core.clipboard.WebClipboardHandler
import com.emarsys.core.db.EmarsysIndexedDb
import com.emarsys.core.db.EmarsysIndexedDbObjectStore
import com.emarsys.core.db.EmarsysObjectStoreConfig
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.db.events.JSEventsDao
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.device.WebPlatformInfoCollector
import com.emarsys.core.language.LanguageTagValidatorApi
import com.emarsys.core.language.WebLanguageTagValidator
import com.emarsys.core.launchapplication.JsLaunchApplicationHandler
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.permission.WebPermissionHandler
import com.emarsys.core.provider.WebApplicationVersionProvider
import com.emarsys.core.provider.WebLanguageProvider
import com.emarsys.core.providers.ApplicationVersionProviderApi
import com.emarsys.core.providers.ClientIdProvider
import com.emarsys.core.providers.LanguageProviderApi
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.url.WebExternalUrlOpener
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppScriptExtractor
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.WebInAppJsBridgeFactory
import com.emarsys.mobileengage.inapp.WebInAppPresenter
import com.emarsys.mobileengage.inapp.WebInAppViewProvider
import com.emarsys.mobileengage.push.PushNotificationClickHandler
import com.emarsys.mobileengage.push.PushNotificationClickHandlerApi
import com.emarsys.mobileengage.push.PushService
import com.emarsys.mobileengage.push.PushServiceContext
import com.emarsys.mobileengage.pushtoinapp.WebPushToInAppHandler
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
        single<SdkConfigStoreApi<JsEmarsysConfig>> {
            JsEmarsysConfigStore(
                typedStorage = get()
            )
        }
        single<LanguageProviderApi> { WebLanguageProvider() }
        single<ApplicationVersionProviderApi> { WebApplicationVersionProvider() }
        single<EventsDaoApi> {
            val emarsysIndexedDb = EmarsysIndexedDb(
                indexedDBFactory = indexedDB,
                sdkLogger = get { parametersOf(EmarsysIndexedDb::class.simpleName) })
            val emarsysIndexedDbObjectStore = EmarsysIndexedDbObjectStore(
                emarsysIndexedDb,
                EmarsysObjectStoreConfig.Events,
                json = get<Json>(),
                logger = get { parametersOf(EmarsysIndexedDbObjectStore::class.simpleName) },
                get(named(DispatcherTypes.Sdk))
            )
            JSEventsDao(
                emarsysIndexedDbObjectStore = emarsysIndexedDbObjectStore,
                logger = get { parametersOf(JSEventsDao::class.simpleName) }
            )
        }
        single<PermissionHandlerApi> { WebPermissionHandler() }
        single<DeviceInfoCollectorApi> {
            DeviceInfoCollector(
                clientIdProvider = ClientIdProvider(uuidProvider = get(), storage = get()),
                timezoneProvider = get(),
                webPlatformInfoCollector = WebPlatformInfoCollector(getNavigatorData()),
                applicationVersionProvider = get(),
                languageProvider = get(),
                wrapperInfoStorage = get(),
                json = get(),
                stringStorage = get(),
                sdkContext = get()
            )
        }
        single<State>(named(StateTypes.PlatformInit)) {
            val pushService = PushService(PushServiceContext(), storage = get<StringStorageApi>())
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
                sdkEventFlow = get(named(EventFlowTypes.InternalEventFlow)),
                coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
                sdkLogger = get { parametersOf(WebBadgeCountHandler::class.simpleName) }

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
        single<PushToInAppHandlerApi> {
            WebPushToInAppHandler(
                downloader = get(),
                inAppHandler = get()
            )
        }
        single<ConnectionWatchDog> { WebConnectionWatchDog(window) }
        single<LifecycleWatchDog> {
            WebLifeCycleWatchDog(
                document = document,
                CoroutineScope(Dispatchers.Default)
            )
        }
        single<FileCacheApi> { WebFileCache() }
        single<InAppViewProviderApi> {
            WebInAppViewProvider(
                InAppScriptExtractor(),
                WebInAppJsBridgeFactory(
                    actionFactory = get<EventActionFactoryApi>(),
                    json = get(),
                    sdkDispatcher = get(named(DispatcherTypes.Sdk))
                )
            )
        }
        single<InAppPresenterApi> {
            WebInAppPresenter(
                sdkEventFlow = get(named(EventFlowTypes.InternalEventFlow)),
                sdkDispatcher = get(named(DispatcherTypes.Sdk))
            )
        }
        single<ClipboardHandlerApi> { WebClipboardHandler(window.navigator.clipboard) }
        single<LaunchApplicationHandlerApi> { JsLaunchApplicationHandler() }
        single<LanguageTagValidatorApi> { WebLanguageTagValidator() }
        single<SdkConfigStoreApi<JsEmarsysConfig>> { JsEmarsysConfigStore(typedStorage = get()) }
        single<PushInstance>(named(InstanceType.Internal)) {
            PushInternal(
                pushClient = get(),
                storage = get(),
                pushContext = get(),
                sdkLogger = get { parametersOf(PushInternal::class.simpleName) }
            )
        }
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