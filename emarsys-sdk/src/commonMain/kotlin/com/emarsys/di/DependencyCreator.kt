package com.emarsys.di

import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushInstance
import com.emarsys.context.SdkContext
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.clipboard.ClipboardHandlerApi
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.log.SdkLogger
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
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.setup.PlatformInitializerApi
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json

interface DependencyCreator {

    fun createPlatformInitializer(
        sdkEventFlow: MutableSharedFlow<Event>,
        pushActionFactory: ActionFactoryApi<ActionModel>,
        pushActionHandler: ActionHandlerApi
    ): PlatformInitializerApi

    fun createPlatformContext(pushActionFactory: ActionFactoryApi<ActionModel>): PlatformContext

    fun createStorage(): TypedStorageApi<String?>

    fun createDeviceInfoCollector(
        timezoneProvider: Provider<String>,
        storage: TypedStorageApi<String?>
    ): DeviceInfoCollector

    fun createPlatformInitState(
        pushApi: PushApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>,
        downloaderApi: DownloaderApi,
        inAppDownloader: InAppDownloaderApi,
        storage: TypedStorageApi<String?>,
        sdkEventFlow: MutableSharedFlow<Event>
    ): State

    fun createPermissionHandler(): PermissionHandlerApi

    fun createExternalUrlOpener(): ExternalUrlOpenerApi

    fun createPushToInAppHandler(
        inAppDownloader: InAppDownloaderApi,
        inAppHandler: InAppHandlerApi
    ): PushToInAppHandlerApi

    fun createConnectionWatchDog(sdkLogger: SdkLogger): ConnectionWatchDog

    fun createLifeCycleWatchDog(): LifecycleWatchDog

    fun createApplicationVersionProvider(): Provider<String>

    fun createLanguageProvider(): Provider<String>

    fun createFileCache(): FileCacheApi

    fun createInAppViewProvider(actionFactory: ActionFactoryApi<ActionModel>): InAppViewProviderApi

    fun createInAppPresenter(): InAppPresenterApi

    fun createClipboardHandler(): ClipboardHandlerApi

    fun createLaunchApplicationHandler(): LaunchApplicationHandlerApi

    fun createPushInternal(
        pushClient: PushClientApi,
        storage: TypedStorageApi<String?>,
        pushContext: ApiContext<PushCall>,
        eventClient: EventClientApi,
        actionFactory: ActionFactoryApi<ActionModel>,
        json: Json,
        sdkDispatcher: CoroutineDispatcher,
        sdkEventFlow: MutableSharedFlow<Event>
    ): PushInstance

    fun createPushApi(
        pushInternal: PushInstance,
        storage: TypedStorageApi<String?>,
        pushContext: ApiContext<PushCall>
    ): PushApi
}