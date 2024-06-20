package com.emarsys.di

import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.emarsys.api.push.PushConstants
import com.emarsys.api.push.PushInternalApi
import com.emarsys.applicationContext
import com.emarsys.context.SdkContext
import com.emarsys.core.badge.AndroidBadgeCountHandler
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.device.AndroidLanguageProvider
import com.emarsys.core.device.AndroidPlatformInfoCollector
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.log.Logger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.permission.AndroidPermissionHandler
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.provider.ApplicationVersionProvider
import com.emarsys.core.providers.HardwareIdProvider
import com.emarsys.core.providers.Provider
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.AndroidExternalUrlOpener
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.push.PushMessageBroadcastReceiver
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.PushTokenBroadcastReceiver
import com.emarsys.setup.PlatformInitState
import com.emarsys.watchdog.connection.AndroidConnectionWatchDog
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.AndroidLifecycleWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import java.util.Locale


actual class PlatformDependencyCreator actual constructor(
    platformContext: PlatformContext,
    private val sdkLogger: Logger,
    private val json: Json
) : DependencyCreator {
    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext
    private val storage = createStorage()

    private fun createLanguageProvider(): LanguageProvider {
        return AndroidLanguageProvider(Locale.getDefault())
    }

    private fun createAndroidDeviceInfoCollector(): AndroidPlatformInfoCollector {
        return AndroidPlatformInfoCollector(applicationContext)
    }

    actual override fun createDeviceInfoCollector(
        uuidProvider: Provider<String>,
        timezoneProvider: Provider<String>
    ): DeviceInfoCollector {
        return DeviceInfoCollector(
            timezoneProvider,
            createLanguageProvider(),
            createApplicationVersionProvider(),
            true,
            HardwareIdProvider(uuidProvider, storage)
        )
    }

    private fun createApplicationVersionProvider(): Provider<String> {
        return ApplicationVersionProvider(applicationContext)
    }

    actual override fun createPlatformInitState(
        pushApi: PushInternalApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>
    ): State {
        val notificationManager =
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        val pushPresenter = PushMessagePresenter(applicationContext, json, notificationManager)
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
        return AndroidExternalUrlOpener()
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

}