package com.emarsys.di

import com.emarsys.api.AppEvent
import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.PushInternalApi
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.clipboard.ClipboardHandlerApi
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.log.Logger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.message.MsgHubApi
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.providers.Provider
import com.emarsys.core.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.state.State
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.setup.PlatformInitializerApi
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json


expect class PlatformDependencyCreator(
    platformContext: PlatformContext,
    sdkContext: SdkContextApi,
    uuidProvider: Provider<String>,
    sdkLogger: Logger,
    json: Json,
    msgHub: MsgHubApi
) : DependencyCreator {

    override fun createPlatformInitializer(pushActionFactory: ActionFactoryApi<ActionModel>): PlatformInitializerApi

    override fun createStorage(): TypedStorageApi<String?>

    override fun createDeviceInfoCollector(
        timezoneProvider: Provider<String>,
        storage: TypedStorageApi<String?>
    ): DeviceInfoCollector

    override fun createPlatformInitState(
        pushApi: PushInternalApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>,
        downloaderApi: DownloaderApi,
        inAppDownloader: InAppDownloaderApi,
        storage: TypedStorageApi<String?>
    ): State

    override fun createPermissionHandler(): PermissionHandlerApi

    override fun createBadgeCountHandler(): BadgeCountHandlerApi

    override fun createExternalUrlOpener(): ExternalUrlOpenerApi

    override fun createPushToInAppHandler(
        inAppDownloader: InAppDownloaderApi,
        inAppHandler: InAppHandlerApi
    ): PushToInAppHandlerApi

    override fun createConnectionWatchDog(sdkLogger: SdkLogger): ConnectionWatchDog

    override fun createLifeCycleWatchDog(): LifecycleWatchDog

    override fun createApplicationVersionProvider(): Provider<String>

    override fun createLanguageProvider(): Provider<String>

    override fun createFileCache(): FileCacheApi

    override fun createInAppViewProvider(actionFactory: ActionFactoryApi<ActionModel>): InAppViewProviderApi

    override fun createInAppPresenter(): InAppPresenterApi

    override fun createClipboardHandler(): ClipboardHandlerApi

    override fun createPushInternal(
        pushClient: PushClientApi,
        storage: TypedStorageApi<String?>,
        pushContext: ApiContext<PushCall>,
        notificationEvents: MutableSharedFlow<AppEvent>,
        eventClient: EventClientApi,
        actionFactory: ActionFactoryApi<ActionModel>,
        json: Json,
        sdkDispatcher: CoroutineDispatcher
    ): PushInstance

    override fun createPushApi(
        pushInternal: PushInstance,
        storage: TypedStorageApi<String?>,
        pushContext: ApiContext<PushCall>,
        notificationEvents: MutableSharedFlow<AppEvent>
    ): PushApi
}