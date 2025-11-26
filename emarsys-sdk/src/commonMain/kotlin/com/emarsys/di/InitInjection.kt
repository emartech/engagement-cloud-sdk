package com.emarsys.di

import com.emarsys.core.Registerable
import com.emarsys.core.channel.SdkEventEmitterApi
import com.emarsys.core.state.State
import com.emarsys.core.state.StateMachine
import com.emarsys.core.state.StateMachineApi
import com.emarsys.enable.states.RegisterEventBasedClientsState
import com.emarsys.init.InitOrganizer
import com.emarsys.init.InitOrganizerApi
import com.emarsys.init.states.ApplyGlobalRemoteConfigState
import com.emarsys.init.states.InitializerState
import com.emarsys.init.states.RegisterEventConsumersState
import com.emarsys.init.states.RegisterInstancesState
import com.emarsys.init.states.RegisterWatchdogsState
import com.emarsys.init.states.RestoreOperationalEventsState
import com.emarsys.init.states.SdkConfigLoaderState
import com.emarsys.init.states.SessionSubscriptionState
import com.emarsys.mobileengage.inapp.InAppEventConsumer
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object InitInjection {
    val initModules = module {
        single<State>(named(InitStateTypes.ApplyGlobalRemoteConfig)) {
            ApplyGlobalRemoteConfigState(
                sdkEventDistributor = get(),
                sdkLogger = get { parametersOf(ApplyGlobalRemoteConfigState::class.simpleName) },
            )
        }
        single<State>(named(InitStateTypes.RegisterInstances)) {
            RegisterInstancesState(
                eventTrackerApi = get(),
                contactApi = get(),
                pushApi = get(),
                inAppApi = get(),
                sdkLogger = get { parametersOf(RegisterInstancesState::class.simpleName) },
            )
        }
        single<State>(named(InitStateTypes.RegisterWatchdogs)) {
            RegisterWatchdogsState(
                lifecycleWatchDog = get<LifecycleWatchDog>() as Registerable,
                connectionWatchDog = get<ConnectionWatchDog>() as Registerable,
                sdkLogger = get { parametersOf(RegisterWatchdogsState::class.simpleName) }
            )
        }
        single<State>(named(InitStateTypes.SessionSubscription)) {
            SessionSubscriptionState(
                emarsysSdkSession = get(),
                lifecycleWatchDog = get(),
                sdkLogger = get { parametersOf(SessionSubscriptionState::class.simpleName) }
            )
        }
        single<State>(named(InitStateTypes.Initializer)) {
            InitializerState(
                platformInitializer = get(),
                sdkLogger = get { parametersOf(InitializerState::class.simpleName) }
            )
        }
        single<State>(named(InitStateTypes.RestoreOperationalEvents)) {
            RestoreOperationalEventsState(
                eventEmitter = get<SdkEventEmitterApi>(),
                eventsDao = get()
            )
        }
        single<State>(named(InitStateTypes.SdkConfigLoader)) {
            SdkConfigLoaderState(
                sdkConfigStore = get(),
                setupOrganizer = get(),
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                sdkLogger = get { parametersOf(SdkConfigLoaderState::class.simpleName) }
            )
        }
        single<State>(named(InitStateTypes.RegisterEventBasedClients)) {
            RegisterEventBasedClientsState(
                clients = listOf(
                    get(named(EventBasedClientTypes.Device)),
                    get(named(EventBasedClientTypes.Config)),
                    get(named(EventBasedClientTypes.DeepLink)),
                    get(named(EventBasedClientTypes.Contact)),
                    get(named(EventBasedClientTypes.Event)),
                    get(named(EventBasedClientTypes.Push)),
                    get(named(EventBasedClientTypes.RemoteConfig)),
                    get(named(EventBasedClientTypes.Logging)),
                    get(named(EventBasedClientTypes.EmbeddedMessaging)),
                    get(named(EventBasedClientTypes.Reregistration)),
                ),
            )
        }
        single<State>(named(InitStateTypes.RegisterEventConsumers)) {
            RegisterEventConsumersState(
                consumers = listOf(
                    get<InAppEventConsumer>()
                )
            )
        }
        single<StateMachineApi>(named(StateMachineTypes.Init)) {
            StateMachine(
                states = listOf(
                    get(named(InitStateTypes.RegisterEventBasedClients)),
                    get(named(InitStateTypes.RegisterEventConsumers)),
                    get(named(InitStateTypes.ApplyGlobalRemoteConfig)),
                    get(named(InitStateTypes.RegisterInstances)),
                    get(named(InitStateTypes.RegisterWatchdogs)),
                    get(named(InitStateTypes.SessionSubscription)),
                    get(named(InitStateTypes.Initializer)),
                    get(named(InitStateTypes.RestoreOperationalEvents)),
                    get(named(InitStateTypes.SdkConfigLoader))
                )
            )
        }
        single<InitOrganizerApi> {
            InitOrganizer(
                initStateMachine = get(named(StateMachineTypes.Init)),
                sdkContext = get(),
                sdkLogger = get { parametersOf(InitOrganizer::class.simpleName) }
            )
        }
    }
}

enum class InitStateTypes {
    ApplyGlobalRemoteConfig, RegisterInstances, RegisterWatchdogs, SessionSubscription, Initializer, SdkConfigLoader, RegisterEventBasedClients, RegisterEventConsumers, RestoreOperationalEvents
}