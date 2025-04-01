package com.emarsys.di

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.net.ConnectivityManager
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.emarsys.SdkConfig
import com.emarsys.api.push.LoggingPush
import com.emarsys.api.push.Push
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushGatherer
import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.PushInternal
import com.emarsys.applicationContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.cache.AndroidFileCache
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.language.LanguageTagValidator
import com.emarsys.core.language.LanguageTagValidatorApi
import com.emarsys.core.launchapplication.LaunchApplicationHandler
import com.emarsys.core.log.Logger
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
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
import com.emarsys.mobileengage.pushtoinapp.PushToInAppHandler
import com.emarsys.mobileengage.url.AndroidExternalUrlOpener
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.setup.config.AndroidSdkConfigStore
import com.emarsys.setup.config.SdkConfigStoreApi
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
import kotlinx.serialization.json.Json
import okio.FileSystem
import org.koin.core.component.inject


internal actual class PlatformDependencyCreator actual constructor(
    private val sdkContext: SdkContextApi,
    private val uuidProvider: UuidProviderApi,
    private val sdkLogger: Logger,
    private val json: Json,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val actionHandler: ActionHandlerApi,
    timestampProvider: InstantProvider
) : DependencyCreator, SdkComponent {
    private val currentActivityWatchdog: TransitionSafeCurrentActivityWatchdog by inject()

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
        return PushToInAppHandler(inAppDownloader, inAppHandler, sdkLogger)
    }

    actual override fun createConnectionWatchDog(sdkLogger: Logger): ConnectionWatchDog {
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

    actual override fun createInAppViewProvider(eventActionFactory: EventActionFactoryApi): InAppViewProviderApi {
        return InAppViewProvider(
            applicationContext,
            InAppJsBridgeProvider(
                eventActionFactory,
                json,
                CoroutineScope(sdkContext.sdkDispatcher)
            ),
            sdkContext.mainDispatcher,
            WebViewProvider(applicationContext, sdkContext.mainDispatcher)
        )
    }

    actual override fun createInAppPresenter(): InAppPresenterApi {
        return InAppPresenter(
            currentActivityWatchdog,
            sdkContext.mainDispatcher,
            sdkContext.sdkDispatcher,
            sdkEventFlow.asSharedFlow(),
            sdkLogger
        )
    }

    actual override fun createClipboardHandler(): ClipboardHandlerApi {
        val clipboardManager =
            applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        return AndroidClipboardHandler(clipboardManager)
    }

    actual override fun createLaunchApplicationHandler(): LaunchApplicationHandlerApi {
        return LaunchApplicationHandler(
            applicationContext,
            currentActivityWatchdog,
            sdkContext,
            sdkLogger
        )
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
        return AndroidSdkConfigStore(typedStorage)
    }
}