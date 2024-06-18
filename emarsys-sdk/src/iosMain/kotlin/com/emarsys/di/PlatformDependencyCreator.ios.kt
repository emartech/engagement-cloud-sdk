package com.emarsys.di

import com.emarsys.api.push.PushInternalApi
import com.emarsys.context.SdkContext
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.log.Logger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.providers.Provider
import com.emarsys.core.state.State
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json

actual class PlatformDependencyCreator actual constructor(
    platformContext: PlatformContext,
    sdkLogger: Logger,
    json: Json
) : DependencyCreator {

    actual override fun createStorage(): TypedStorageApi<String?> {
        TODO("Not yet implemented")
    }

    actual override fun createDeviceInfoCollector(
        uuidProvider: Provider<String>,
        timezoneProvider: Provider<String>
    ): DeviceInfoCollector {
        TODO("Not yet implemented")
    }

    actual override fun createPlatformInitState(
        pushApi: PushInternalApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>
    ): State {
        TODO("Not yet implemented")
    }

    actual override fun createPermissionHandler(): PermissionHandlerApi {
        TODO("Not yet implemented")
    }

    actual override fun createBadgeCountHandler(): BadgeCountHandlerApi {
        TODO("Not yet implemented")
    }

    actual override fun createExternalUrlOpener(): ExternalUrlOpenerApi {
        TODO("Not yet implemented")
    }

    actual override fun createConnectionWatchDog(sdkLogger: SdkLogger): ConnectionWatchDog {
        TODO("Not yet implemented")
    }

    actual override fun createLifeCycleWatchDog(): LifecycleWatchDog {
        TODO("Not yet implemented")
    }

}