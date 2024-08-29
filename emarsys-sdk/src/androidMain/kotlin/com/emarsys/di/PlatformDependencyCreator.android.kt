package com.emarsys.di

import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.emarsys.api.push.PushConstants
import com.emarsys.api.push.PushInternalApi
import com.emarsys.applicationContext
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.badge.AndroidBadgeCountHandler
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.cache.AndroidFileCache
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.clipboard.AndroidClipboardHandler
import com.emarsys.core.clipboard.ClipboardHandlerApi
import com.emarsys.core.device.AndroidLanguageProvider
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.PlatformInfoCollector
import com.emarsys.core.log.Logger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.message.MsgHubApi
import com.emarsys.core.permission.AndroidPermissionHandler
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.provider.AndroidApplicationVersionProvider
import com.emarsys.core.providers.HardwareIdProvider
import com.emarsys.core.providers.Provider
import com.emarsys.core.resource.MetadataReader
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.AndroidExternalUrlOpener
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.inapp.InAppJsBridgeProvider
import com.emarsys.mobileengage.inapp.InAppPresenter
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewProvider
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.WebViewProvider
import com.emarsys.mobileengage.push.NotificationCompatStyler
import com.emarsys.mobileengage.push.PushMessageBroadcastReceiver
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.PushTokenBroadcastReceiver
import com.emarsys.setup.PlatformInitState
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import com.emarsys.watchdog.connection.AndroidConnectionWatchDog
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.AndroidLifecycleWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okio.FileSystem
import java.util.Locale


actual class PlatformDependencyCreator actual constructor(
    platformContext: PlatformContext,
    private val sdkContext: SdkContextApi,
    private val uuidProvider: Provider<String>,
    private val sdkLogger: Logger,
    private val json: Json,
    private val msgHub: MsgHubApi
) : DependencyCreator {
    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext
    private val metadataReader = MetadataReader(applicationContext)
    private val storage = createStorage()
    private val platformInfoCollector = PlatformInfoCollector(applicationContext)

    private val currentActivityWatchdog =
        TransitionSafeCurrentActivityWatchdog().also { it.register() }

    actual override fun createLanguageProvider(): Provider<String> {
        return AndroidLanguageProvider(Locale.getDefault())
    }


    actual override fun createDeviceInfoCollector(
        timezoneProvider: Provider<String>
    ): DeviceInfoCollector {
        return DeviceInfoCollector(
            timezoneProvider,
            createLanguageProvider(),
            createApplicationVersionProvider(),
            true,
            HardwareIdProvider(uuidProvider, storage),
            platformInfoCollector,
            json
        )
    }

    actual override fun createApplicationVersionProvider(): Provider<String> {
        return AndroidApplicationVersionProvider(applicationContext)
    }

    actual override fun createPlatformInitState(
        pushApi: PushInternalApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>,
        downloaderApi: DownloaderApi
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
            platformInfoCollector
        )
        val pushTokenBroadcastReceiver = PushTokenBroadcastReceiver(sdkDispatcher, pushApi)
        val pushMessageBroadcastReceiver =
            PushMessageBroadcastReceiver(pushPresenter, sdkDispatcher, sdkLogger, json)
        return PlatformInitState(
            pushTokenBroadcastReceiver,
            IntentFilter(PushConstants.PUSH_TOKEN_INTENT_FILTER_ACTION),
            pushMessageBroadcastReceiver,
            IntentFilter(PushConstants.PUSH_MESSAGE_PAYLOAD_INTENT_FILTER_ACTION),
            applicationContext,
        )
    }

    actual override fun createStorage(): TypedStorageApi<String?> =
        StringStorage(platformContext.sharedPreferences)

    actual override fun createPermissionHandler(): PermissionHandlerApi {
        return AndroidPermissionHandler()
    }

    actual override fun createBadgeCountHandler(): BadgeCountHandlerApi {
        return AndroidBadgeCountHandler()
    }

    actual override fun createExternalUrlOpener(): ExternalUrlOpenerApi {
        return AndroidExternalUrlOpener(applicationContext)
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
        return InAppPresenter(currentActivityWatchdog, msgHub)
    }

    actual override fun createClipboardHandler(): ClipboardHandlerApi {
        val clipboardManager =
            applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        return AndroidClipboardHandler(clipboardManager)
    }
}