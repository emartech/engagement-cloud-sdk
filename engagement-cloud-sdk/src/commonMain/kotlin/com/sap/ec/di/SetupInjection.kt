package com.sap.ec.di

import com.sap.ec.core.channel.SdkEventEmitterApi
import com.sap.ec.core.state.State
import com.sap.ec.core.state.StateMachine
import com.sap.ec.core.state.StateMachineApi
import com.sap.ec.disable.DisableOrganizer
import com.sap.ec.disable.DisableOrganizerApi
import com.sap.ec.disable.states.ClearEventsState
import com.sap.ec.disable.states.ClearPushTokenOnDisableState
import com.sap.ec.disable.states.ClearStoredConfigState
import com.sap.ec.enable.EnableOrganizer
import com.sap.ec.enable.EnableOrganizerApi
import com.sap.ec.enable.states.AppStartState
import com.sap.ec.enable.states.ApplyAppCodeBasedRemoteConfigState
import com.sap.ec.enable.states.CollectDeviceInfoState
import com.sap.ec.enable.states.FetchEmbeddedMessagingMetaState
import com.sap.ec.enable.states.RegisterClientState
import com.sap.ec.enable.states.RegisterPushTokenState
import com.sap.ec.enable.states.RestoreSavedSdkEventsState
import com.sap.ec.mobileengage.config.FollowUpChangeAppCodeOrganizer
import com.sap.ec.mobileengage.config.FollowUpChangeAppCodeOrganizerApi
import com.sap.ec.networking.clients.EventBasedClientApi
import com.sap.ec.networking.clients.reregistration.ReregistrationClient
import com.sap.ec.reregistration.states.ClearRequestContextTokensState
import com.sap.ec.reregistration.states.LinkContactState
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
                sdkEventDistributor = get(),
                sdkContext = get()
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
        single<State>(named(StateTypes.FetchEmbeddedMessagingMetaState)) {
            FetchEmbeddedMessagingMetaState(
                embeddedMessagingContext = get(),
                sdkEventDistributor = get(),
                sdkContext = get(),
                sdkLogger = get { parametersOf(FetchEmbeddedMessagingMetaState::class.simpleName) }
            )
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
                    get<State>(named(StateTypes.AppStart)),
                    get<State>(named(StateTypes.FetchEmbeddedMessagingMetaState)),
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
        single<StateMachineApi>(named(StateMachineTypes.FollowUpChangeAppCodeStateMachine)) {
            StateMachine(
                states = listOf(
                    get<State>(named(StateTypes.ApplyAppCodeBasedRemoteConfig)),
                    get<State>(named(StateTypes.FetchEmbeddedMessagingMetaState))
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
                ecSdkSession = get(),
                sdkLogger = get { parametersOf(EnableOrganizer::class.simpleName) }
            )
        }
        single<DisableOrganizerApi> {
            DisableOrganizer(
                mobileEngageDisableStateMachine = get(named(StateMachineTypes.MobileEngageDisable)),
                sdkContext = get(),
                ecSdkSession = get(),
                sdkLogger = get { parametersOf(DisableOrganizer::class.simpleName) },
            )
        }
        single<FollowUpChangeAppCodeOrganizerApi> {
            FollowUpChangeAppCodeOrganizer(
                followUpChangeAppCodeStateMachine = get(named(StateMachineTypes.FollowUpChangeAppCodeStateMachine)),
                sdkContext = get(),
                logger = get { parametersOf(FollowUpChangeAppCodeOrganizer::class.simpleName) }
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
    MobileEngageEnable, MobileEngageDisable, Init, MobileEngageReregistration, FollowUpChangeAppCodeStateMachine
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
    FetchEmbeddedMessagingMetaState,
}