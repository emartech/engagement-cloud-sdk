package com.emarsys.di

import com.emarsys.mobileengage.action.ActionCommandFactoryApi
import com.emarsys.api.push.PushInternalApi
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.state.State
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.providers.Provider
import kotlinx.coroutines.CoroutineDispatcher

interface DependencyCreator {
    fun createStorage():  TypedStorageApi<String?>
    fun createActionCommandFactory(): ActionCommandFactoryApi

    fun createDeviceInfoCollector(uuidProvider: Provider<String>): DeviceInfoCollector

    fun createPlatformInitState(pushApi: PushInternalApi, sdkDispatcher: CoroutineDispatcher): State

}