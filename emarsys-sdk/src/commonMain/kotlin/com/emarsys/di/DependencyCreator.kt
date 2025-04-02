package com.emarsys.di

import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushInstance
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.push.PushClientApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json

internal interface DependencyCreator {

    fun createPushInternal(
        pushClient: PushClientApi,
        storage: StringStorageApi,
        pushContext: PushContextApi,
        eventClient: EventClientApi,
        pushActionFactory: PushActionFactoryApi,
        json: Json,
        sdkDispatcher: CoroutineDispatcher
    ): PushInstance

    fun createPushApi(
        pushInternal: PushInstance,
        storage: StringStorageApi,
        pushContext: PushContextApi
    ): PushApi
}