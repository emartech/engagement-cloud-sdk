package com.emarsys.di

import com.emarsys.api.push.LoggingPush
import com.emarsys.api.push.Push
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushGatherer
import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.PushInternal
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.push.PushClientApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json


internal actual class PlatformDependencyCreator actual constructor(
    private val sdkContext: SdkContextApi,
    private val uuidProvider: UuidProviderApi,
    private val sdkLogger: Logger,
    private val json: Json,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val actionHandler: ActionHandlerApi,
    timestampProvider: InstantProvider
) : DependencyCreator, SdkComponent {

    actual override fun createPushInternal(
        pushClient: PushClientApi,
        storage: StringStorageApi,
        pushContext: PushContextApi,
        eventClient: EventClientApi,
        pushActionFactory: PushActionFactoryApi,
        json: Json,
        sdkDispatcher: CoroutineDispatcher
    ): PushInstance {
        return PushInternal(pushClient, storage, pushContext)
    }

    actual override fun createPushApi(
        pushInternal: PushInstance,
        storage: StringStorageApi,
        pushContext: PushContextApi,
    ): PushApi {
        val loggingPush = LoggingPush(storage, sdkLogger)
        val pushGatherer = PushGatherer(pushContext, storage)
        return Push(loggingPush, pushGatherer, pushInternal, sdkContext)
    }

}