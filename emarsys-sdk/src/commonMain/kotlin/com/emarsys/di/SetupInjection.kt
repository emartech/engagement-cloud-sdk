package com.emarsys.di

import com.emarsys.core.channel.SdkEventEmitterApi
import com.emarsys.core.state.State
import com.emarsys.core.state.StateMachine
import com.emarsys.core.state.StateMachineApi
import com.emarsys.disable.DisableOrganizer
import com.emarsys.disable.DisableOrganizerApi
import com.emarsys.disable.states.ClearEvents
import com.emarsys.disable.states.ClearStoredConfig
import com.emarsys.enable.EnableOrganizer
import com.emarsys.enable.EnableOrganizerApi
import com.emarsys.enable.states.AppStartState
import com.emarsys.enable.states.ApplyAppCodeBasedRemoteConfigState
import com.emarsys.enable.states.CollectDeviceInfoState
import com.emarsys.enable.states.RegisterClientState
import com.emarsys.enable.states.RegisterPushTokenState
import com.emarsys.enable.states.RestoreSavedSdkEventsState
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.reregistration.ReregistrationClient
import com.emarsys.reregistration.states.ClearSessionContextState
import com.emarsys.reregistration.states.LinkContactState
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object SetupInjection {
    val setupModules = module {
        single<State>(named(StateTypes.CollectDeviceInfo)) {
            CollectDeviceInfoState(
                deviceInfoCollector = get(),
                sessionContext = get()
            )
        }
        single<State>(named(StateTypes.RegisterPushToken)) {
            RegisterPushTokenState(
                storage = get(),
                sdkEventDistributor = get(),
            )
        }
        single<State>(named(StateTypes.RegisterClient)) {
            RegisterClientState(
                sdkEventDistributor = get()
            )
        }
        single<State>(named(StateTypes.ApplyAppCodeBasedRemoteConfig)) {
            ApplyAppCodeBasedRemoteConfigState(sdkEventDistributor = get())
        }
        single<State>(named(StateTypes.RestoreSavedSdkEvents)) {
            RestoreSavedSdkEventsState(
                eventsDao = get(),
                sdkEventEmitter = get<SdkEventEmitterApi>(),
                sdkLogger = get { parametersOf(RestoreSavedSdkEventsState::class.simpleName) }
            )
        }
        single<State>(named(StateTypes.AppStart)) {
            AppStartState(
                sdkEventDistributor = get(),
                timestampProvider = get(),
                uuidProvider = get()
            )
        }
        single<State>(named(StateTypes.ClearSessionContext)) {
            ClearSessionContextState(
                sessionContext = get(),
                sdkLogger = get { parametersOf(ClearSessionContextState::class.simpleName) }
            )
        }
        single<State>(named(StateTypes.LinkContact)) {
            LinkContactState(
                sdkContext = get(),
                sdkEventDistributor = get(),
                sdkLogger = get { parametersOf(LinkContactState::class.simpleName) }
            )
        }
        single<State>(named(StateTypes.ClearStoredConfig)) {
            ClearStoredConfig(
                sdkConfigStore = get(),
                sdkLogger = get { parametersOf(ClearStoredConfig::class.simpleName) }
            )
        }
        single<State>(named(StateTypes.ClearEvents)) {
            ClearEvents(eventsDao = get())
        }

        single<StateMachineApi>(named(StateMachineTypes.MobileEngageEnable)) {
            StateMachine(
                states = listOf(
                    get<State>(named(StateTypes.CollectDeviceInfo)),
                    get<State>(named(StateTypes.RegisterClient)),
                    get<State>(named(StateTypes.ApplyAppCodeBasedRemoteConfig)),
                    get<State>(named(StateTypes.PlatformInit)),
                    get<State>(named(StateTypes.RegisterPushToken)),
                    get<State>(named(StateTypes.RestoreSavedSdkEvents)),
                    get<State>(named(StateTypes.AppStart))
                )
            )
        }
        single<StateMachineApi>(named(StateMachineTypes.PredictEnable)) {
            StateMachine(
                states = listOf(
                    get<State>(named(StateTypes.CollectDeviceInfo)),
                    get<State>(named(StateTypes.PlatformInit)),
                    get<State>(named(StateTypes.RestoreSavedSdkEvents)),
                )
            )
        }
        single<StateMachineApi>(named(StateMachineTypes.MobileEngageReregistration)) {
            StateMachine(
                states = listOf(
                    get<State>(named(StateTypes.ClearSessionContext)),
                    get<State>(named(StateTypes.RegisterClient)),
                    get<State>(named(StateTypes.ApplyAppCodeBasedRemoteConfig)),
                    get<State>(named(StateTypes.RegisterPushToken)),
                    get<State>(named(StateTypes.LinkContact))
                )
            )
        }
        single<StateMachineApi>(named(StateMachineTypes.PredictOnlyReregistration)) {
            StateMachine(
                states = listOf(
                    get<State>(named(StateTypes.ClearSessionContext)),
                    get<State>(named(StateTypes.LinkContact))
                )
            )
        }
        single<StateMachineApi>(named(StateMachineTypes.MobileEngageDisable)) {
            StateMachine(
                states = listOf(
                    get<State>(named(StateTypes.ClearEvents)),
                    get<State>(named(StateTypes.ClearStoredConfig)),
                )
            )
        }
        single<StateMachineApi>(named(StateMachineTypes.PredictDisable)) {
            StateMachine(
                states = listOf(
                    get<State>(named(StateTypes.ClearEvents)),
                    get<State>(named(StateTypes.ClearStoredConfig)),
                )
            )
        }
        single<EnableOrganizerApi> {
            EnableOrganizer(
                meStateMachine = get(named(StateMachineTypes.MobileEngageEnable)),
                predictStateMachine = get(named(StateMachineTypes.PredictEnable)),
                sdkContext = get(),
                sdkLogger = get { parametersOf(EnableOrganizer::class.simpleName) },
                sdkConfigStore = get()
            )
        }
        single<DisableOrganizerApi> {
            DisableOrganizer(
                mobileEngageDisableStateMachine = get(named(StateMachineTypes.MobileEngageEnable)),
                predictDisableStateMachine = get(named(StateMachineTypes.PredictEnable)),
                sdkContext = get(),
                sdkLogger = get { parametersOf(EnableOrganizer::class.simpleName) },
            )
        }

        single<EventBasedClientApi>(named(EventBasedClientTypes.Reregistration)) {
            ReregistrationClient(
                sdkEventManager = get(),
                sdkContext = get(),
                mobileEngageReregistrationStateMachine = get(named(StateMachineTypes.MobileEngageReregistration)),
                predictOnlyReregistrationStateMachine = get(named(StateMachineTypes.PredictOnlyReregistration)),
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                sdkLogger = get { parametersOf(ReregistrationClient::class.simpleName) })
        }
    }
}

enum class StateMachineTypes {
    MobileEngageEnable, PredictEnable, MobileEngageDisable, PredictDisable, Init, MobileEngageReregistration, PredictOnlyReregistration
}

enum class StateTypes {
    CollectDeviceInfo,
    ApplyAppCodeBasedRemoteConfig,
    PlatformInit,
    RegisterClient,
    RegisterPushToken,
    AppStart,
    RestoreSavedSdkEvents,
    ClearSessionContext,
    LinkContact,
    ClearStoredConfig,
    ClearEvents,
}