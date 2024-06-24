package com.emarsys.di

import com.emarsys.api.push.PushInternalApi
import com.emarsys.context.SdkContext
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.device.DeviceInfoCollector
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

interface DependencyCreator {
    fun createStorage(): TypedStorageApi<String?>

    fun createDeviceInfoCollector(
        timezoneProvider: Provider<String>
    ): DeviceInfoCollector

    fun createPlatformInitState(
        pushApi: PushInternalApi,
        sdkDispatcher: CoroutineDispatcher,
        sdkContext: SdkContext,
        actionFactory: ActionFactoryApi<ActionModel>
    ): State

    fun createPermissionHandler(): PermissionHandlerApi

    fun createBadgeCountHandler(): BadgeCountHandlerApi

    fun createExternalUrlOpener(): ExternalUrlOpenerApi
    fun createConnectionWatchDog(sdkLogger: SdkLogger): ConnectionWatchDog

    fun createLifeCycleWatchDog(): LifecycleWatchDog

    fun createApplicationVersionProvider(): Provider<String>

    fun createLanguageProvider(): Provider<String>
}