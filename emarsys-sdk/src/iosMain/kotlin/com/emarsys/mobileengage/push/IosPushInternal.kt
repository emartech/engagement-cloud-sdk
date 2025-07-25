package com.emarsys.mobileengage.push

import com.emarsys.SdkConstants.SILENT_PUSH_RECEIVED_EVENT_NAME
import com.emarsys.api.SdkState
import com.emarsys.api.push.PushCall.ClearPushToken
import com.emarsys.api.push.PushCall.HandleSilentMessageWithUserInfo
import com.emarsys.api.push.PushCall.RegisterPushToken
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushInternal
import com.emarsys.api.push.PushUserInfo
import com.emarsys.api.push.SilentPushUserInfo
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.actions.badge.BadgeCountHandlerApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.collections.dequeue
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.InAppActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.push.extension.toPushUserInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationDefaultActionIdentifier
import platform.UserNotifications.UNNotificationPresentationOptionAlert
import platform.UserNotifications.UNNotificationPresentationOptionBadge
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class IosPushInternal(
    storage: StringStorageApi,
    private val pushContext: PushContextApi,
    sdkContext: SdkContextApi,
    private val actionFactory: PushActionFactoryApi,
    private val actionHandler: ActionHandlerApi,
    private val badgeCountHandler: BadgeCountHandlerApi,
    private val json: Json,
    private val sdkDispatcher: CoroutineDispatcher,
    private val sdkLogger: Logger,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val timestampProvider: InstantProvider,
    private val uuidProvider: UuidProviderApi
) : PushInternal(storage, pushContext, sdkEventDistributor, sdkLogger), IosPushInstance {
    //TODO: should handle list in a threadsafe way
    override var customerUserNotificationCenterDelegate: List<UNUserNotificationCenterDelegateProtocol> =
        listOf()
        set(value) {
            (emarsysUserNotificationCenterDelegate as InternalNotificationCenterDelegateProxy).customerDelegates =
                value
            field = value
        }

    override val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol =
        InternalNotificationCenterDelegateProxy(
            didReceiveNotificationResponse = this::didReceiveNotificationResponse,
            sdkContext,
            customerUserNotificationCenterDelegate
        )

    override suspend fun handleSilentMessageWithUserInfo(userInfo: SilentPushUserInfo) {
        userInfo.notification.actions?.forEach {
            actionFactory.create(it).invoke()
        }

        //TODO: revisit what we want to send in attributes after API discovery
        sdkEventDistributor.registerEvent(
            SdkEvent.External.Api.AppEvent(
                id = uuidProvider.provide(),
                name = SILENT_PUSH_RECEIVED_EVENT_NAME,
                timestamp = timestampProvider.provide()
            )
        )
    }

    override suspend fun activate() {
        pushContext.calls.dequeue { call ->
            when (call) {
                is RegisterPushToken -> sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Sdk.RegisterPushToken(pushToken = call.pushToken)
                )

                is ClearPushToken -> sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.ClearPushToken())
                is HandleSilentMessageWithUserInfo -> handleSilentMessageWithUserInfo(call.userInfo)
            }
        }
    }

    fun didReceiveNotificationResponse(
        actionIdentifier: String,
        userInfo: Map<String, Any>,
        withCompletionHandler: () -> Unit
    ) {
        CoroutineScope(sdkDispatcher).launch {
            try {
                val pushUserInfo = userInfo.toPushUserInfo(json)
                pushUserInfo?.let {
                    handleActions(actionIdentifier, pushUserInfo)
                    pushUserInfo.notification.badgeCount?.let { badgeCountHandler.handle(it) }

                    withContext(Dispatchers.Main) {
                        withCompletionHandler()
                    }
                }
            } catch (exception: Exception) {
                sdkLogger.error("DidReceiveNotificationResponse", exception)
            }
        }
    }

    private suspend fun handleActions(
        actionIdentifier: String,
        pushUserInfo: PushUserInfo
    ) {
        val actionModel: ActionModel? =
            if (actionIdentifier == UNNotificationDefaultActionIdentifier) {
                pushUserInfo.notification.defaultAction
            } else {
                pushUserInfo.notification.actions?.firstOrNull {
                    it.id == actionIdentifier
                }
            }

        if (actionModel is InAppActionModel) {
            actionModel.trackingInfo = pushUserInfo.ems.trackingInfo
        }

        val triggeredAction = actionModel?.let {
            actionFactory.create(it)
        }

        val mandatoryActions = createMandatoryActions(pushUserInfo, actionModel)

        actionHandler.handleActions(mandatoryActions, triggeredAction)
    }


    private suspend fun createMandatoryActions(
        pushUserInfo: PushUserInfo,
        actionModel: ActionModel?
    ): List<Action<*>> {
        return buildList {
            val model = when (actionModel) {
                is PresentableActionModel -> {
                    BasicPushButtonClickedActionModel(
                        actionModel.reporting,
                        pushUserInfo.ems.trackingInfo
                    )
                }

                is BasicActionModel, null -> NotificationOpenedActionModel(
                    actionModel?.reporting,
                    pushUserInfo.ems.trackingInfo
                )

                else -> null
            }

            model?.let { add(actionFactory.create(model)) }
        }
    }

    private class InternalNotificationCenterDelegateProxy(
        private val didReceiveNotificationResponse: (actionIdentifier: String, userInfo: Map<String, Any>, handler: () -> Unit) -> Unit,
        private val sdkContext: SdkContextApi,
        var customerDelegates: List<UNUserNotificationCenterDelegateProtocol> = emptyList()
    ) : UNUserNotificationCenterDelegateProtocol, NSObject() {

        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            willPresentNotification: UNNotification,
            withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                customerDelegates.forEach {
                    it.userNotificationCenter(
                        center,
                        willPresentNotification,
                        withCompletionHandler
                    )
                }
            }
            if (sdkContext.currentSdkState.value == SdkState.active) {
                withCompletionHandler(UNNotificationPresentationOptionBanner + UNNotificationPresentationOptionList + UNNotificationPresentationOptionAlert + UNNotificationPresentationOptionSound + UNNotificationPresentationOptionBadge)
            }
        }

        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            didReceiveNotificationResponse: UNNotificationResponse,
            withCompletionHandler: () -> Unit
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                customerDelegates.forEach {
                    it.userNotificationCenter(
                        center,
                        didReceiveNotificationResponse,
                        withCompletionHandler
                    )
                }
            }
            if (sdkContext.currentSdkState.value == SdkState.active) {
                this.didReceiveNotificationResponse(
                    didReceiveNotificationResponse.actionIdentifier,
                    didReceiveNotificationResponse.notification.request.content.userInfo as Map<String, Any>,
                    withCompletionHandler
                )
            }
        }

        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            openSettingsForNotification: UNNotification?
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                customerDelegates.forEach {
                    it.userNotificationCenter(center, openSettingsForNotification)
                }
            }
        }
    }
}

