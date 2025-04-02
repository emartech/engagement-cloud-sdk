package com.emarsys.di

import com.emarsys.SdkConfig
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushInstance
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.language.LanguageTagValidatorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.setup.config.SdkConfigStoreApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json


internal expect class PlatformDependencyCreator(
    sdkContext: SdkContextApi,
    uuidProvider: UuidProviderApi,
    sdkLogger: Logger,
    json: Json,
    sdkEventFlow: MutableSharedFlow<SdkEvent>,
    actionHandler: ActionHandlerApi,
    timestampProvider: InstantProvider
) : DependencyCreator {

    override fun createClipboardHandler(): ClipboardHandlerApi

    override fun createLaunchApplicationHandler(): LaunchApplicationHandlerApi

    override fun createLanguageTagValidator(): LanguageTagValidatorApi


    override fun createPushInternal(
        pushClient: PushClientApi,
        storage: StringStorageApi,
        pushContext: PushContextApi,
        eventClient: EventClientApi,
        pushActionFactory: PushActionFactoryApi,
        json: Json,
        sdkDispatcher: CoroutineDispatcher
    ): PushInstance

    override fun createPushApi(
        pushInternal: PushInstance,
        storage: StringStorageApi,
        pushContext: PushContextApi,
    ): PushApi

    override fun createSdkConfigStore(typedStorage: TypedStorageApi): SdkConfigStoreApi<SdkConfig>
}