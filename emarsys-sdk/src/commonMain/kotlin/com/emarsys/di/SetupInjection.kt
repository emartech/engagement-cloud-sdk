package com.emarsys.di

import com.emarsys.core.channel.SdkEventEmitterApi
import com.emarsys.core.state.State
import com.emarsys.core.state.StateMachine
import com.emarsys.core.state.StateMachineApi
import com.emarsys.disable.DisableOrganizer
import com.emarsys.disable.DisableOrganizerApi
import com.emarsys.disable.states.ClearEventsState
import com.emarsys.disable.states.ClearPushTokenOnDisableState
import com.emarsys.disable.states.ClearStoredConfigState
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
import com.emarsys.reregistration.states.ClearRequestContextTokensState
import com.emarsys.reregistration.states.LinkContactState
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object SetupInjection {
    val setupModules = module {
        single<State>(named(StateTypes.CollectDeviceInfo)) {
            CollectDeviceInfoState(
                deviceInfoCollector = get(),
                requestContext = get()
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
        single<State>(named(StateTypes.ClearRequestContextTokens)) {
            ClearRequestContextTokensState(
                requestContext = get(),
                sdkLogger = get { parametersOf(ClearRequestContextTokensState::class.simpleName) }
            )
        }
        single<State>(named(StateTypes.ClearPushTokenOnDisable)) {
            ClearPushTokenOnDisableState(
                sdkEventDistributor = get()
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
            ClearStoredConfigState(
                sdkConfigStore = get(),
                sdkLogger = get { parametersOf(ClearStoredConfigState::class.simpleName) }
            )
        }
        single<State>(named(StateTypes.ClearEvents)) {
            ClearEventsState(eventsDao = get())
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
        single<StateMachineApi>(named(StateMachineTypes.MobileEngageReregistration)) {
            StateMachine(
                states = listOf(
                    get<State>(named(StateTypes.ClearRequestContextTokens)),
                    get<State>(named(StateTypes.RegisterClient)),
                    get<State>(named(StateTypes.ApplyAppCodeBasedRemoteConfig)),
                    get<State>(named(StateTypes.RegisterPushToken)),
                    get<State>(named(StateTypes.LinkContact))
                )
            )
        }
        single<StateMachineApi>(named(StateMachineTypes.MobileEngageDisable)) {
            StateMachine(
                states = listOf(
                    get<State>(named(StateTypes.ClearPushTokenOnDisable)),
                    get<State>(named(StateTypes.ClearEvents)),
                    get<State>(named(StateTypes.ClearStoredConfig)),
                )
            )
        }
        single<EnableOrganizerApi> {
            EnableOrganizer(
                meStateMachine = get(named(StateMachineTypes.MobileEngageEnable)),
                sdkContext = get(),
                sdkConfigStore = get(),
                emarsysSdkSession = get(),
                sdkLogger = get { parametersOf(EnableOrganizer::class.simpleName) }
            )
        }
        single<DisableOrganizerApi> {
            DisableOrganizer(
                mobileEngageDisableStateMachine = get(named(StateMachineTypes.MobileEngageDisable)),
                sdkContext = get(),
                emarsysSdkSession = get(),
                sdkLogger = get { parametersOf(EnableOrganizer::class.simpleName) },
            )
        }

        single<EventBasedClientApi>(named(EventBasedClientTypes.Reregistration)) {
            ReregistrationClient(
                sdkEventManager = get(),
                sdkContext = get(),
                mobileEngageReregistrationStateMachine = get(named(StateMachineTypes.MobileEngageReregistration)),
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                sdkLogger = get { parametersOf(ReregistrationClient::class.simpleName) })
        }
    }
}

enum class StateMachineTypes {
    MobileEngageEnable, MobileEngageDisable, Init, MobileEngageReregistration
}

enum class StateTypes {
    CollectDeviceInfo,
    ApplyAppCodeBasedRemoteConfig,
    PlatformInit,
    RegisterClient,
    RegisterPushToken,
    AppStart,
    RestoreSavedSdkEvents,
    ClearRequestContextTokens,
    ClearPushTokenOnDisable,
    LinkContact,
    ClearStoredConfig,
    ClearEvents,
}