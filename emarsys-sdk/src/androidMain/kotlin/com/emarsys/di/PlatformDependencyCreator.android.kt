package com.emarsys.di

import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import com.emarsys.SdkConfig
import com.emarsys.api.push.LoggingPush
import com.emarsys.api.push.Push
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushGatherer
import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.PushInternal
import com.emarsys.applicationContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.language.LanguageTagValidator
import com.emarsys.core.language.LanguageTagValidatorApi
import com.emarsys.core.launchapplication.LaunchApplicationHandler
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.mobileengage.clipboard.AndroidClipboardHandler
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.setup.config.AndroidSdkConfigStore
import com.emarsys.setup.config.SdkConfigStoreApi
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import org.koin.core.component.inject


internal actual class PlatformDependencyCreator actual constructor(
    private val sdkContext: SdkContextApi,
    private val uuidProvider: UuidProviderApi,
    private val sdkLogger: Logger,
    private val json: Json,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val actionHandler: ActionHandlerApi,
    timestampProvider: InstantProvider
) : DependencyCreator, SdkComponent {
    private val currentActivityWatchdog: TransitionSafeCurrentActivityWatchdog by inject()

    actual override fun createClipboardHandler(): ClipboardHandlerApi {
        val clipboardManager =
            applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        return AndroidClipboardHandler(clipboardManager)
    }

    actual override fun createLaunchApplicationHandler(): LaunchApplicationHandlerApi {
        return LaunchApplicationHandler(
            applicationContext,
            currentActivityWatchdog,
            sdkContext,
            sdkLogger
        )
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
        return AndroidSdkConfigStore(typedStorage)
    }
}