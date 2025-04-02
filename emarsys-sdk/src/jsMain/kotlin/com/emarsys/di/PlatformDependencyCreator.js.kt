package com.emarsys.di

import com.emarsys.SdkConfig
import com.emarsys.api.push.LoggingPush
import com.emarsys.api.push.Push
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushGatherer
import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.PushInternal
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.clipboard.WebClipboardHandler
import com.emarsys.core.language.LanguageTagValidator
import com.emarsys.core.language.LanguageTagValidatorApi
import com.emarsys.core.launchapplication.JsLaunchApplicationHandler
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.mobileengage.inapp.InAppJsBridgeFactory
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppScriptExtractor
import com.emarsys.mobileengage.inapp.InAppScriptExtractorApi
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.WebInAppPresenter
import com.emarsys.mobileengage.inapp.WebInAppViewProvider
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.setup.config.JsEmarsysConfigStore
import com.emarsys.setup.config.SdkConfigStoreApi
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json

internal actual class PlatformDependencyCreator actual constructor(
    private val sdkContext: SdkContextApi,
    private val uuidProvider: UuidProviderApi,
    private val sdkLogger: Logger,
    private val json: Json,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    actionHandler: ActionHandlerApi,
    timestampProvider: InstantProvider
) : DependencyCreator {

    private val inappScriptExtractor: InAppScriptExtractorApi by lazy {
        InAppScriptExtractor()
    }

    actual override fun createInAppViewProvider(eventActionFactory: EventActionFactoryApi): InAppViewProviderApi {
        return WebInAppViewProvider(
            inappScriptExtractor,
            InAppJsBridgeFactory(eventActionFactory, json, Dispatchers.Main)
        )
    }

    actual override fun createInAppPresenter(): InAppPresenterApi {
        return WebInAppPresenter(sdkEventFlow, sdkContext.sdkDispatcher)
    }

    actual override fun createClipboardHandler(): ClipboardHandlerApi {
        return WebClipboardHandler(window.navigator.clipboard)
    }

    actual override fun createLaunchApplicationHandler(): LaunchApplicationHandlerApi {
        return JsLaunchApplicationHandler()
    }

    actual override fun createLanguageTagValidator(): LanguageTagValidatorApi {
        return LanguageTagValidator()
    }

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

    actual override fun createSdkConfigStore(typedStorage: TypedStorageApi): SdkConfigStoreApi<SdkConfig> {
        return JsEmarsysConfigStore(typedStorage)
    }
}