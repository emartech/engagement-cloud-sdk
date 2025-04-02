package com.emarsys.di

import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.networking.clients.event.model.SdkEvent
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

}