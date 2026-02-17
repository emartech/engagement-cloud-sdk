package com.sap.ec.di

import com.sap.ec.api.push.PushCall
import com.sap.ec.api.push.PushContext
import com.sap.ec.api.push.PushContextApi
import com.sap.ec.core.actions.ActionHandler
import com.sap.ec.core.actions.ActionHandlerApi
import com.sap.ec.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.sap.ec.core.collections.PersistentList
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.mobileengage.action.PushActionFactory
import com.sap.ec.mobileengage.action.PushActionFactoryApi
import com.sap.ec.mobileengage.pushtoinapp.PushToInAppHandler
import com.sap.ec.networking.clients.EventBasedClientApi
import com.sap.ec.networking.clients.push.PushClient
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object PushInjection {
    val pushModules = module {
        singleOf(::PushActionFactory) { bind<PushActionFactoryApi>() }
        singleOf(::ActionHandler) { bind<ActionHandlerApi>() }
        single<PushToInAppHandlerApi> {
            PushToInAppHandler(
                downloader = get(),
                sdkLogger = get { parametersOf(PushToInAppHandler::class.simpleName) },
                sdkEventManager = get()
            )
        }
        single<EventBasedClientApi>(named(EventBasedClientTypes.Push)) {
            PushClient(
                ecClient = get<NetworkClientApi>(named(NetworkClientTypes.EC)),
                clientExceptionHandler = get(),
                urlFactory = get<UrlFactoryApi>(),
                sdkEventManager = get(),
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                eventsDao = get(),
                json = get<Json>(),
                sdkLogger = get { parametersOf(PushClient::class.simpleName) }
            )
        }
        single<MutableList<PushCall>>(named(PersistentListTypes.PushCall)) {
            PersistentList(
                id = PersistentListIds.PUSH_CONTEXT_PERSISTENT_ID,
                storage = get(),
                elementSerializer = PushCall.serializer(),
                elements = listOf()
            )
        }
        single<PushContextApi> {
            PushContext(
                calls = get(named(PersistentListTypes.PushCall))
            )
        }
    }
}

enum class InstanceType {
    Logging, Internal, Gatherer
}
