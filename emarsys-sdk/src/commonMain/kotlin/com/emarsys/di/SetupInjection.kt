package com.emarsys.di

import com.emarsys.core.channel.SdkEventEmitterApi
import com.emarsys.core.state.State
import com.emarsys.core.state.StateMachine
import com.emarsys.core.state.StateMachineApi
import com.emarsys.setup.SetupOrganizer
import com.emarsys.setup.SetupOrganizerApi
import com.emarsys.setup.states.AppStartState
import com.emarsys.setup.states.ApplyAppCodeBasedRemoteConfigState
import com.emarsys.setup.states.CollectDeviceInfoState
import com.emarsys.setup.states.RegisterClientState
import com.emarsys.setup.states.RegisterPushTokenState
import com.emarsys.setup.states.RestoreSavedSdkEventsState
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
            ApplyAppCodeBasedRemoteConfigState(
                remoteConfigHandler = get()
            )
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
        single<StateMachineApi>(named(StateMachineTypes.ME)) {
            StateMachine(
                states = listOf(
                    get<State>(named(StateTypes.CollectDeviceInfo)),
                    get<State>(named(StateTypes.ApplyAppCodeBasedRemoteConfig)),
                    get<State>(named(StateTypes.PlatformInit)),
                    get<State>(named(StateTypes.RegisterClient)),
                    get<State>(named(StateTypes.RegisterPushToken)),
                    get<State>(named(StateTypes.RestoreSavedSdkEvents)),
                    get<State>(named(StateTypes.AppStart))
                )
            )
        }
        single<StateMachineApi>(named(StateMachineTypes.Predict)) {
            StateMachine(
                states = listOf(
                    get<State>(named(StateTypes.CollectDeviceInfo)),
                    get<State>(named(StateTypes.PlatformInit)),
                    get<State>(named(StateTypes.RestoreSavedSdkEvents)),
                )
            )
        }
        single<SetupOrganizerApi> {
            SetupOrganizer(
                meStateMachine = get(named(StateMachineTypes.ME)),
                predictStateMachine = get(named(StateMachineTypes.Predict)),
                sdkContext = get(),
                sdkLogger = get { parametersOf(SetupOrganizer::class.simpleName) },
                sdkConfigStore = get()
            )
        }
    }
}

enum class StateMachineTypes {
    ME, Predict, Init
}

enum class StateTypes {
    CollectDeviceInfo, ApplyAppCodeBasedRemoteConfig, PlatformInit, RegisterClient, RegisterPushToken, AppStart, RestoreSavedSdkEvents
}