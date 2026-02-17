package com.sap.ec.mobileengage.push

import com.sap.ec.SdkConstants.SILENT_PUSH_RECEIVED_EVENT_NAME
import com.sap.ec.api.SdkState
import com.sap.ec.api.push.PushCall.ClearPushToken
import com.sap.ec.api.push.PushCall.HandleSilentMessageWithUserInfo
import com.sap.ec.api.push.PushCall.RegisterPushToken
import com.sap.ec.api.push.PushContextApi
import com.sap.ec.api.push.PushInternal
import com.sap.ec.api.push.PushUserInfo
import com.sap.ec.api.push.SilentPushUserInfo
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.actions.ActionHandlerApi
import com.sap.ec.core.actions.badge.BadgeCountHandlerApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.collections.dequeue
import com.sap.ec.core.log.Logger
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.action.PushActionFactoryApi
import com.sap.ec.mobileengage.action.actions.Action
import com.sap.ec.mobileengage.action.models.ActionModel
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.sap.ec.mobileengage.action.models.NotificationOpenedActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.mobileengage.push.extension.toPushUserInfo
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
) : PushInternal(storage, pushContext, sdkEventDistributor, sdkContext, sdkLogger),
    IosPushInstance {
    //TODO: should handle list in a threadsafe way
    override var customerUserNotificationCenterDelegate: List<UNUserNotificationCenterDelegateProtocol> =
        listOf()
        set(value) {
            (userNotificationCenterDelegate as InternalNotificationCenterDelegateProxy).customerDelegates =
                value
            field = value
        }

    override val userNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol =
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

                is ClearPushToken -> sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Sdk.ClearPushToken(
                        applicationCode = call.applicationCode
                    )
                )

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
            if (sdkContext.currentSdkState.value == SdkState.Active) {
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
            if (sdkContext.currentSdkState.value == SdkState.Active) {
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

