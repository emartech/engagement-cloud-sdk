package com.emarsys.di

import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushInstance
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.actions.badge.BadgeCountHandlerApi
import com.emarsys.core.badge.IosBadgeCountHandler
import com.emarsys.core.device.UIDevice
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.mobileengage.push.IosGathererPush
import com.emarsys.mobileengage.push.IosLoggingPush
import com.emarsys.mobileengage.push.IosPush
import com.emarsys.mobileengage.push.IosPushInternal
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.push.PushClientApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import platform.Foundation.NSProcessInfo
import platform.UserNotifications.UNUserNotificationCenter

internal actual class PlatformDependencyCreator actual constructor(
    private val sdkContext: SdkContextApi,
    private val uuidProvider: UuidProviderApi,
    private val sdkLogger: Logger,
    private val json: Json,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val actionHandler: ActionHandlerApi,
    private val timestampProvider: InstantProvider,
) : DependencyCreator, SdkComponent {
    private val processInfo = NSProcessInfo()
    private val uiDevice = UIDevice(processInfo)
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    private val badgeCountHandler: BadgeCountHandlerApi =
        IosBadgeCountHandler(notificationCenter, uiDevice, sdkContext.mainDispatcher)

    actual override fun createPushInternal(
        pushClient: PushClientApi,
        storage: StringStorageApi,
        pushContext: PushContextApi,
        eventClient: EventClientApi,
        pushActionFactory: PushActionFactoryApi,
        json: Json,
        sdkDispatcher: CoroutineDispatcher
    ): PushInstance {
        return IosPushInternal(
            pushClient,
            storage,
            pushContext,
            sdkContext,
            pushActionFactory,
            actionHandler,
            badgeCountHandler,
            json,
            sdkDispatcher,
            sdkLogger,
            sdkEventFlow,
            timestampProvider,
            uuidProvider
        )
    }

    actual override fun createPushApi(
        pushInternal: PushInstance,
        storage: StringStorageApi,
        pushContext: PushContextApi,
    ): PushApi {
        val loggingPush = IosLoggingPush(sdkLogger, storage, sdkContext.sdkDispatcher)
        val pushGatherer = IosGathererPush(pushContext, storage, pushInternal as IosPushInternal)
        return IosPush(
            loggingPush,
            pushGatherer,
            pushInternal,
            sdkContext,
            sdkLogger
        )
    }

}