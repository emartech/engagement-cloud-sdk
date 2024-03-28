package com.emarsys.di

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.emarsys.api.push.PushConstants
import com.emarsys.api.push.PushInternalApi
import com.emarsys.applicationContext
import com.emarsys.core.badge.AndroidBadgeCountHandler
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.connection.AndroidConnectionWatchDog
import com.emarsys.core.device.AndroidLanguageProvider
import com.emarsys.core.device.AndroidPlatformInfoCollector
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.permission.AndroidPermissionHandler
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.provider.ApplicationVersionProvider
import com.emarsys.core.providers.Provider
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.AndroidExternalUrlOpener
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.push.PushTokenBroadcastReceiver
import com.emarsys.setup.PlatformInitState
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import java.util.Locale


actual class PlatformDependencyCreator actual constructor(platformContext: PlatformContext) :
    DependencyCreator {
    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext


    private fun createLanguageProvider(): LanguageProvider {
        return AndroidLanguageProvider(Locale.getDefault())
    }

    private fun createAndroidDeviceInfoCollector(): AndroidPlatformInfoCollector {
        return AndroidPlatformInfoCollector(applicationContext)
    }

    override fun createDeviceInfoCollector(
        uuidProvider: Provider<String>,
        timezoneProvider: Provider<String>
    ): DeviceInfoCollector {
        return DeviceInfoCollector(
            uuidProvider,
            timezoneProvider,
            createLanguageProvider(),
            createApplicationVersionProvider(),
            createStorage(),
            true
        )
    }

    private fun createApplicationVersionProvider(): Provider<String> {
        return ApplicationVersionProvider(applicationContext)
    }

    override fun createPlatformInitState(
        pushApi: PushInternalApi,
        sdkDispatcher: CoroutineDispatcher
    ): State {
        val receiver = PushTokenBroadcastReceiver(sdkDispatcher, pushApi)
        return PlatformInitState(
            receiver,
            IntentFilter(PushConstants.PUSH_TOKEN_INTENT_FILTER_ACTION),
            applicationContext
        )
    }

    override fun createStorage(): TypedStorageApi<String?> =
        StringStorage(platformContext.sharedPreferences)

    override fun createPermissionHandler(): PermissionHandlerApi {
        return AndroidPermissionHandler()
    }

    override fun createBadgeCountHandler(): BadgeCountHandlerApi {
        return AndroidBadgeCountHandler()
    }

    override fun createExternalUrlOpener(): ExternalUrlOpenerApi {
        return AndroidExternalUrlOpener()
    }

    override fun createConnectionWatchDog(sdkLogger: SdkLogger): AndroidConnectionWatchDog {
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return AndroidConnectionWatchDog(connectivityManager, sdkLogger)
    }

    override fun createLifeCycleWatchDog(): LifecycleWatchDog {
        TODO("Not yet implemented")
    }

}