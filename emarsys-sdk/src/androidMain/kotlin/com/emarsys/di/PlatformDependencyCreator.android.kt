package com.emarsys.di

import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.LoggingPush
import com.emarsys.api.push.Push
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushConstants
import com.emarsys.api.push.PushGatherer
import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.PushInternal
import com.emarsys.applicationContext
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.cache.AndroidFileCache
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.clipboard.ClipboardHandlerApi
import com.emarsys.core.device.AndroidLanguageProvider
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.PlatformInfoCollector
import com.emarsys.core.launchapplication.LaunchApplicationHandler
import com.emarsys.core.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.provider.AndroidApplicationVersionProvider
import com.emarsys.core.providers.ClientIdProvider
import com.emarsys.core.providers.Provider
import com.emarsys.core.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.resource.MetadataReader
import com.emarsys.core.state.State
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.clipboard.AndroidClipboardHandler
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import com.emarsys.mobileengage.inapp.InAppJsBridgeProvider
import com.emarsys.mobileengage.inapp.InAppPresenter
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewProvider
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.WebViewProvider
import com.emarsys.mobileengage.permission.AndroidPermissionHandler
import com.emarsys.mobileengage.push.NotificationCompatStyler
import com.emarsys.mobileengage.push.PushMessageBroadcastReceiver
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.SilentPushMessageHandler
import com.emarsys.mobileengage.pushtoinapp.PushToInAppHandler
import com.emarsys.mobileengage.url.AndroidExternalUrlOpener
import com.emarsys.networking.clients.event.EventClientApi

import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.setup.PlatformInitState
import com.emarsys.setup.PlatformInitializer
import com.emarsys.setup.PlatformInitializerApi
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import com.emarsys.watchdog.connection.AndroidConnectionWatchDog
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.AndroidLifecycleWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import okio.FileSystem
import java.util.Locale


actual class PlatformDependencyCreator actual constructor(
    private val sdkContext: SdkContextApi,
    private val uuidProvider: Provider<String>,
    private val sdkLogger: Logger,
    private val json: Json,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val actionHandler: ActionHandlerApi,
    private val timestampProvider: Provider<Instant>
) : DependencyCreator {
    private val metadataReader = MetadataReader(applicationContext)
    private val platformInfoCollector = PlatformInfoCollector(applicationContext)
    private val currentActivityWatchdog =
        TransitionSafeCurrentActivityWatchdog().also { it.register() }
    private val sharedPreferences =
        applicationContext.getSharedPreferences(StorageConstants.SUITE_NAME, Context.MODE_PRIVATE)

    actual override fun createPlatformInitializer(
        pushActionFactory: ActionFactoryApi<ActionModel>,
        pushActionHandler: ActionHandlerApi
    ): PlatformInitializerApi {
        return PlatformInitializer()
    }

    actual override fun createPlatformContext(pushActionFactory: ActionFactoryApi<ActionModel>): PlatformContext {
        return AndroidPlatformContext(json, pushActionFactory, actionHandler)
    }

    actual override fun createLanguageProvider(): Provider<String> {
        return AndroidLanguageProvider(Locale.getDefault())
    }

    actual override fun createDeviceInfoCollector(
        timezoneProvider: Provider<String>,
        storage: TypedStorageApi<String?>
    ): DeviceInfoCollector {
        return DeviceInfoCollector(
            timezoneProvider,
            createLanguageProvider(),
            createApplicationVersionProvider(),
            true,
            ClientIdProvider(uuidProvider, storage),
            platformInfoCollector,
            json
        )
    }

    actual override fun createApplicationVersionProvider(): Provider<String> {
        return AndroidApplicationVersionProvider(applicationContext)
    }

    actual override fun createPlatformInitState(
        pushApi: PushApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>,
        downloaderApi: DownloaderApi,
        inAppDownloader: InAppDownloaderApi,
        storage: TypedStorageApi<String?>
    ): State {
        val notificationManager =
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        val notificationCompatStyler = NotificationCompatStyler(downloaderApi)
        val pushPresenter = PushMessagePresenter(
            applicationContext,
            json,
            notificationManager,
            metadataReader,
            notificationCompatStyler,
            platformInfoCollector,
            inAppDownloader
        )
        val silentPushHandler = SilentPushMessageHandler(actionFactory, sdkEventFlow)
        val pushMessageBroadcastReceiver =
            PushMessageBroadcastReceiver(
                pushPresenter,
                silentPushHandler,
                sdkDispatcher,
                sdkLogger,
                json,
            )
        return PlatformInitState(
            pushMessageBroadcastReceiver,
            IntentFilter(PushConstants.PUSH_MESSAGE_PAYLOAD_INTENT_FILTER_ACTION),
            applicationContext,
        )
    }

    actual override fun createStorage(): TypedStorageApi<String?> =
        StringStorage(sharedPreferences)

    actual override fun createPermissionHandler(): PermissionHandlerApi {
        return AndroidPermissionHandler(applicationContext, currentActivityWatchdog)
    }

    actual override fun createExternalUrlOpener(): ExternalUrlOpenerApi {
        return AndroidExternalUrlOpener(applicationContext, sdkLogger)
    }

    actual override fun createPushToInAppHandler(
        inAppDownloader: InAppDownloaderApi,
        inAppHandler: InAppHandlerApi
    ): PushToInAppHandlerApi {
        return PushToInAppHandler(inAppDownloader, inAppHandler)
    }

    actual override fun createConnectionWatchDog(sdkLogger: SdkLogger): ConnectionWatchDog {
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return AndroidConnectionWatchDog(connectivityManager, sdkLogger)
    }

    actual override fun createLifeCycleWatchDog(): LifecycleWatchDog {
        return AndroidLifecycleWatchDog(
            ProcessLifecycleOwner.get().lifecycle,
            ProcessLifecycleOwner.get().lifecycleScope,
            CoroutineScope(Dispatchers.Default)
        )
    }

    actual override fun createFileCache(): FileCacheApi {
        return AndroidFileCache(applicationContext, FileSystem.SYSTEM)
    }

    actual override fun createInAppViewProvider(actionFactory: ActionFactoryApi<ActionModel>): InAppViewProviderApi {
        return InAppViewProvider(
            applicationContext,
            InAppJsBridgeProvider(actionFactory, json, CoroutineScope(sdkContext.sdkDispatcher)),
            sdkContext.mainDispatcher,
            WebViewProvider(applicationContext, sdkContext.mainDispatcher)
        )
    }

    actual override fun createInAppPresenter(): InAppPresenterApi {
        return InAppPresenter(currentActivityWatchdog, sdkEventFlow.asSharedFlow())
    }

    actual override fun createClipboardHandler(): ClipboardHandlerApi {
        val clipboardManager =
            applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        return AndroidClipboardHandler(clipboardManager)
    }

    actual override fun createLaunchApplicationHandler(): LaunchApplicationHandlerApi {
        return LaunchApplicationHandler(applicationContext, currentActivityWatchdog, sdkContext)
    }

    actual override fun createPushInternal(
        pushClient: PushClientApi,
        storage: TypedStorageApi<String?>,
        pushContext: ApiContext<PushCall>,
        eventClient: EventClientApi,
        actionFactory: ActionFactoryApi<ActionModel>,
        json: Json,
        sdkDispatcher: CoroutineDispatcher
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
}