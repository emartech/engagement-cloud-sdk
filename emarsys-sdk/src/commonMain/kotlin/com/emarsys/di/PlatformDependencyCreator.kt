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


expect class PlatformDependencyCreator(
    platformContext: PlatformContext,
    uuidProvider: Provider<String>,
    sdkLogger: Logger,
    json: Json
) : DependencyCreator {
    override fun createStorage(): TypedStorageApi<String?>

    override fun createDeviceInfoCollector(
        timezoneProvider: Provider<String>
    ): DeviceInfoCollector

    override fun createPlatformInitState(
        pushApi: PushInternalApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>
    ): State

    override fun createPermissionHandler(): PermissionHandlerApi

    override fun createBadgeCountHandler(): BadgeCountHandlerApi

    override fun createExternalUrlOpener(): ExternalUrlOpenerApi

    override fun createConnectionWatchDog(sdkLogger: SdkLogger): ConnectionWatchDog

    override fun createLifeCycleWatchDog(): LifecycleWatchDog

    override fun createApplicationVersionProvider(): Provider<String>

    override fun createLanguageProvider(): Provider<String>
}