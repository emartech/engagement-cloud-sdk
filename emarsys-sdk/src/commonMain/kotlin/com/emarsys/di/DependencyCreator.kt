package com.emarsys.di

import com.emarsys.SdkConfig
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushInstance
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.language.LanguageTagValidatorApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.setup.config.SdkConfigStoreApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json

internal interface DependencyCreator {

    fun createClipboardHandler(): ClipboardHandlerApi

    fun createLaunchApplicationHandler(): LaunchApplicationHandlerApi

    fun createLanguageTagValidator(): LanguageTagValidatorApi

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

    fun createSdkConfigStore(typedStorage: TypedStorageApi): SdkConfigStoreApi<SdkConfig>
}