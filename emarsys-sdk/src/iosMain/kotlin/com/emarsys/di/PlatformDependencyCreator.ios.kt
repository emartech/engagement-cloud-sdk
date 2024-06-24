package com.emarsys.di

import com.emarsys.api.push.PushInternalApi
import com.emarsys.context.SdkContext
import com.emarsys.core.IosBadgeCountHandler
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.UIDevice
import com.emarsys.core.log.Logger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.permission.IosPermissionHandler
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.provider.IosApplicationVersionProvider
import com.emarsys.core.provider.IosLanguageProvider
import com.emarsys.core.providers.HardwareIdProvider
import com.emarsys.core.providers.Provider
import com.emarsys.core.setup.PlatformInitState
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.url.IosExternalUrlOpener
import com.emarsys.core.watchdog.connection.IosConnectionWatchdog
import com.emarsys.core.watchdog.lifecycle.IosLifecycleWatchdog
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json

actual class PlatformDependencyCreator actual constructor(
    platformContext: PlatformContext,
    private val uuidProvider: Provider<String>,
    sdkLogger: Logger,
    private val json: Json
) : DependencyCreator {

    actual override fun createStorage(): TypedStorageApi<String?> {
        return StringStorage()
    }

    actual override fun createDeviceInfoCollector(
        timezoneProvider: Provider<String>
    ): DeviceInfoCollector {
        return DeviceInfoCollector(
            HardwareIdProvider(uuidProvider, createStorage()),
            createApplicationVersionProvider(),
            createLanguageProvider(),
            timezoneProvider,
            UIDevice(),
            json,
        )
    }

    actual override fun createPlatformInitState(
        pushApi: PushInternalApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>
    ): State {
        return PlatformInitState()
    }

    actual override fun createPermissionHandler(): PermissionHandlerApi {
        return IosPermissionHandler()
    }

    actual override fun createBadgeCountHandler(): BadgeCountHandlerApi {
        return IosBadgeCountHandler()
    }

    actual override fun createExternalUrlOpener(): ExternalUrlOpenerApi {
        return IosExternalUrlOpener()
    }

    actual override fun createConnectionWatchDog(sdkLogger: SdkLogger): ConnectionWatchDog {
        return IosConnectionWatchdog()
    }

    actual override fun createLifeCycleWatchDog(): LifecycleWatchDog {
        return IosLifecycleWatchdog()
    }

    actual override fun createApplicationVersionProvider(): Provider<String> {
        return IosApplicationVersionProvider()
    }

    actual override fun createLanguageProvider(): Provider<String> {
        return IosLanguageProvider()
    }

}