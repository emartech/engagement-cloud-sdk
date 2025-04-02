package com.emarsys.di

import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.networking.clients.event.model.SdkEvent
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

}