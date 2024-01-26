package com.emarsys.di

import com.emarsys.api.push.PushApi
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.state.State
import com.emarsys.core.storage.StorageApi
import com.emarsys.providers.Provider
import kotlinx.coroutines.CoroutineDispatcher

interface DependencyCreator {
    fun createStorage():  StorageApi<String?>

    fun createDeviceInfoCollector(uuidProvider: Provider<String>): DeviceInfoCollector

    fun createPlatformInitState(pushApi: PushApi, sdkDispatcher: CoroutineDispatcher): State

}