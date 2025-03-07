package com.emarsys.mobileengage.push

import com.emarsys.SdkConstants.PUSH_RECEIVED_EVENT_NAME
import com.emarsys.api.SdkState
import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.BasicPushUserInfo
import com.emarsys.api.push.PresentablePushUserInfo
import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushCall.ClearPushToken
import com.emarsys.api.push.PushCall.HandleMessageWithUserInfo
import com.emarsys.api.push.PushCall.RegisterPushToken
import com.emarsys.api.push.PushInternal
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.collections.dequeue
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.Provider
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel

import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.push.PushClientApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSJSONWritingPrettyPrinted
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
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

class IosPushInternal(
    private val pushClient: PushClientApi,
    storage: TypedStorageApi<String?>,
    private val pushContext: ApiContext<PushCall>,
    sdkContext: SdkContextApi,
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val actionHandler: ActionHandlerApi,
    private val badgeCountHandler: BadgeCountHandlerApi,
    private val json: Json,
    private val sdkDispatcher: CoroutineDispatcher,
    private val sdkLogger: Logger,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val timestampProvider: Provider<Instant>,
    private val uuidProvider: Provider<String>
) : PushInternal(pushClient, storage, pushContext, sdkLogger), IosPushInstance {
    override var customerUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol? =
        null
        set(value) {
            (emarsysUserNotificationCenterDelegate as InternalNotificationCenterDelegateProxy).customerDelegate =
                value
            field = value
        }

    override val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol =
        InternalNotificationCenterDelegateProxy(
            didReceiveNotificationResponse = this::didReceiveNotificationResponse,
            sdkContext,
            customerUserNotificationCenterDelegate
        )

    override suspend fun handleSilentMessageWithUserInfo(userInfo: BasicPushUserInfo) {
        userInfo.ems.actions?.forEach {
            actionFactory.create(it).invoke()
        }

        sdkEventFlow.emit(
            SdkEvent.External.Api.SilentPush(
                id = uuidProvider.provide(),
                name = PUSH_RECEIVED_EVENT_NAME,
                attributes = buildJsonObject {
                    put(
                        "campaignId",
                        JsonPrimitive(userInfo.ems.multichannelId)
                    )
                },
                timestamp = timestampProvider.provide()
            )
        )
    }

    override suspend fun activate() {
        pushContext.calls.dequeue { call ->
            when (call) {
                is RegisterPushToken -> pushClient.registerPushToken(call.pushToken)
                is ClearPushToken -> pushClient.clearPushToken()
                is HandleMessageWithUserInfo -> handleSilentMessageWithUserInfo(call.userInfo)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun Map<String, Any>.toPresentableUserInfo(): PresentablePushUserInfo {
        val userInfoString = NSString.create(
            NSJSONSerialization.dataWithJSONObject(
                this,
                NSJSONWritingPrettyPrinted,
                null
            )!!, NSUTF8StringEncoding
        ).toString()
        return json.decodeFromString<PresentablePushUserInfo>(userInfoString)
    }

    fun didReceiveNotificationResponse(
        actionIdentifier: String,
        userInfo: Map<String, Any>,
        withCompletionHandler: () -> Unit
    ) {
        CoroutineScope(sdkDispatcher).launch {
            try {
                val pushUserInfo = userInfo.toPresentableUserInfo()
                handleActions(actionIdentifier, pushUserInfo)
                pushUserInfo.ems?.badgeCount?.let { badgeCountHandler.handle(it) }

                withContext(Dispatchers.Main) {
                    withCompletionHandler()
                }
            } catch (exception: Exception) {
                sdkLogger.error("IosPushInternal - didReceiveNotificationResponse", exception)
            }
        }
    }

    private suspend fun handleActions(
        actionIdentifier: String,
        pushUserInfo: PresentablePushUserInfo
    ) {
        val actionModel: ActionModel? =
            if (actionIdentifier == UNNotificationDefaultActionIdentifier) {
                extractDefaultAction(pushUserInfo)
            } else {
                pushUserInfo.ems?.actions?.firstOrNull {
                    it.id == actionIdentifier
                }
            }

        actionModel?.let {
            val triggeredAction = actionFactory.create(it)
            val mandatoryActions = createMandatoryActions(pushUserInfo, it)
            actionHandler.handleActions(mandatoryActions, triggeredAction)
        }
    }

    private suspend fun createMandatoryActions(
        pushUserInfo: PresentablePushUserInfo,
        actionModel: ActionModel
    ): List<Action<*>> {
        val result = mutableListOf<Action<*>>()
        val sid: String? = pushUserInfo.u?.sid ?: pushUserInfo.ems?.sid

        sid?.let {
            val model = when (actionModel) {
                is PresentableActionModel -> {
                    BasicPushButtonClickedActionModel(
                        actionModel.id,
                        it
                    )
                }

                is BasicActionModel -> {
                    NotificationOpenedActionModel(it)
                }

                else -> null
            }

            model?.let { result.add(actionFactory.create(model)) }
        }
        return result
    }

    private fun extractDefaultAction(
        pushUserInfo: PresentablePushUserInfo,
    ): ActionModel? {
        return if (pushUserInfo.ems?.inapp != null) {
            InternalPushToInappActionModel(
                campaignId = pushUserInfo.ems.inapp.campaignId,
                url = pushUserInfo.ems.inapp.url
            )
        } else {
            pushUserInfo.ems?.defaultAction
        }
    }

    private class InternalNotificationCenterDelegateProxy(
        private val didReceiveNotificationResponse: (actionIdentifier: String, userInfo: Map<String, Any>, handler: () -> Unit) -> Unit,
        private val sdkContext: SdkContextApi,
        var customerDelegate: UNUserNotificationCenterDelegateProtocol?
    ) : UNUserNotificationCenterDelegateProtocol, NSObject() {

        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            willPresentNotification: UNNotification,
            withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
        ) {
            customerDelegate?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    it.userNotificationCenter(
                        center,
                        willPresentNotification,
                        withCompletionHandler
                    )
                }
            }
            if (sdkContext.currentSdkState == SdkState.active) {
                withCompletionHandler(UNNotificationPresentationOptionBanner + UNNotificationPresentationOptionList + UNNotificationPresentationOptionAlert + UNNotificationPresentationOptionSound + UNNotificationPresentationOptionBadge)
            }
        }

        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            didReceiveNotificationResponse: UNNotificationResponse,
            withCompletionHandler: () -> Unit
        ) {
            customerDelegate?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    it.userNotificationCenter(
                        center,
                        didReceiveNotificationResponse,
                        withCompletionHandler
                    )
                }
            }
            if (sdkContext.currentSdkState == SdkState.active) {
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
            customerDelegate?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    it.userNotificationCenter(center, openSettingsForNotification)
                }
            }
        }
    }
}
