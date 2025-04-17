package com.emarsys.di

import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushContext
import com.emarsys.api.push.PushContextApi
import com.emarsys.core.actions.ActionHandler
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.collections.PersistentList
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.mobileengage.action.PushActionFactory
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.push.PushClient
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
        single<EventBasedClientApi>(named(EventBasedClientTypes.Push)) {
            PushClient(
                emarsysClient = get<NetworkClientApi>(named(NetworkClientTypes.Emarsys)),
                urlFactory = get<UrlFactoryApi>(),
                sdkEventManager = get(),
                eventsDao = get(),
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                sdkLogger = get { parametersOf(PushClient::class.simpleName) },
                json = get<Json>()
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
