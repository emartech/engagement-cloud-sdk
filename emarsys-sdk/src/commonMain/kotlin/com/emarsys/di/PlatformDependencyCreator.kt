package com.emarsys.di

import com.emarsys.SdkConfig
import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushInstance
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.log.Logger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.providers.Provider
import com.emarsys.core.state.State
import com.emarsys.core.storage.SuspendTypedStorageApi
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
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.setup.PlatformInitializerApi
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json


expect class PlatformDependencyCreator(
    sdkContext: SdkContextApi,
    uuidProvider: Provider<String>,
    sdkLogger: Logger,
    json: Json,
    sdkEventFlow: MutableSharedFlow<SdkEvent>,
    actionHandler: ActionHandlerApi,
    timestampProvider: Provider<Instant>
) : DependencyCreator {

    override fun createPlatformInitializer(
        pushActionFactory: ActionFactoryApi<ActionModel>,
        pushActionHandler: ActionHandlerApi
    ): PlatformInitializerApi

    override fun createPlatformContext(
        pushActionFactory: ActionFactoryApi<ActionModel>,
        downloaderApi: DownloaderApi,
        inAppDownloader: InAppDownloaderApi,
    ): PlatformContext

    override fun createStorage(): TypedStorageApi<String?>

    override fun createSdkConfigStorage(): SuspendTypedStorageApi<SdkConfig?>

    override fun createEventsDao(): EventsDaoApi

    override fun createDeviceInfoCollector(
        timezoneProvider: Provider<String>,
        storage: TypedStorageApi<String?>
    ): DeviceInfoCollector

    override fun createPlatformInitState(
        pushApi: PushApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>,
        storage: TypedStorageApi<String?>
    ): State

    override fun createPermissionHandler(): PermissionHandlerApi

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

    override fun createLaunchApplicationHandler(): LaunchApplicationHandlerApi

    override fun createPushInternal(
        pushClient: PushClientApi,
        storage: TypedStorageApi<String?>,
        pushContext: ApiContext<PushCall>,
        eventClient: EventClientApi,
        actionFactory: ActionFactoryApi<ActionModel>,
        json: Json,
        sdkDispatcher: CoroutineDispatcher
    ): PushInstance

    override fun createPushApi(
        pushInternal: PushInstance,
        storage: TypedStorageApi<String?>,
        pushContext: ApiContext<PushCall>,
    ): PushApi
}