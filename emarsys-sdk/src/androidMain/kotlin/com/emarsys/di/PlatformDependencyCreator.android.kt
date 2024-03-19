package com.emarsys.di

import android.content.IntentFilter
import com.emarsys.api.push.PushConstants
import com.emarsys.api.push.PushInternalApi
import com.emarsys.applicationContext
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.device.AndroidLanguageProvider
import com.emarsys.core.device.AndroidPlatformInfoCollector
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.providers.Provider
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.push.PushTokenBroadcastReceiver
import com.emarsys.setup.PlatformInitState
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

    override fun createDeviceInfoCollector(uuidProvider: Provider<String>): DeviceInfoCollector {
        return DeviceInfoCollector(
            createAndroidDeviceInfoCollector(),
            createLanguageProvider(),
            uuidProvider,
            createStorage(),
            true,
        )
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
        TODO("Not yet implemented")
    }

    override fun createBadgeCountHandler(): BadgeCountHandlerApi {
        TODO("Not yet implemented")
    }

    override fun createExternalUrlOpener(): ExternalUrlOpenerApi {
        TODO("Not yet implemented")
    }

}