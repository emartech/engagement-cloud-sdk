package com.emarsys.mobileengage.push

import com.emarsys.api.AppEvent
import com.emarsys.api.push.PushInformation
import com.emarsys.api.push.PushInternalApi
import com.emarsys.api.push.PushType
import com.emarsys.mobileengage.action.ActionFactory
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationDefaultActionIdentifier
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

class PushOfficer(
    private val pushApi: PushInternalApi,
    private val actionFactory: ActionFactory<ActionModel>,
    private val eventClient: EventClientApi,
    private val json: Json,
    private val sdkDispatcher: CoroutineDispatcher
) : PushInternalApi, UNUserNotificationCenterDelegateProtocol, NSObject() {

    private val _pushInformation = MutableSharedFlow<PushInformation>()
    val pushInformation = _pushInformation.asSharedFlow()

    var userNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol? = null

    override suspend fun registerPushToken(pushToken: String) {
        pushApi.registerPushToken(pushToken)
    }

    override suspend fun clearPushToken() {
        pushApi.clearPushToken()
    }

    override val pushToken: String?
        get() = pushApi.pushToken

    override val notificationEvents: MutableSharedFlow<AppEvent>
        get() = pushApi.notificationEvents

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
    ) {
        userNotificationCenterDelegate?.let {
            CoroutineScope(Dispatchers.Main).launch {
                it.userNotificationCenter(center, willPresentNotification, withCompletionHandler)
            }
        }
        withCompletionHandler(UNNotificationPresentationOptionBanner + UNNotificationPresentationOptionList)
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        userNotificationCenterDelegate?.let {
            CoroutineScope(Dispatchers.Main).launch {
                it.userNotificationCenter(
                    center,
                    didReceiveNotificationResponse,
                    withCompletionHandler
                )
            }
        }
        CoroutineScope(sdkDispatcher).launch {
            val userInfo = didReceiveNotificationResponse.notification.request.content.userInfo
            val ems = userInfo["ems"] as? Map<String, Any>

            ems?.get("multichannelId")?.let {
                _pushInformation.tryEmit(PushInformation(it as String, PushType.Push))
            }

            ems?.get("inapp")?.let {
                // TODO: show inApp message
            }

            var action: Map<String, Any>? = null
            ems?.get("default_action")?.let {
                if (didReceiveNotificationResponse.actionIdentifier == UNNotificationDefaultActionIdentifier) {
                    action = it as Map<String, Any>
                }
            }
            (ems?.get("actions") as? List<Map<String, Any>>)?.let { actionMap ->
                action = actionMap.firstOrNull {
                    didReceiveNotificationResponse.actionIdentifier == it["id"]
                }
            }
            val actionModel: ActionModel? = action?.let {
                val actionModelString = json.encodeToString(it)
                json.decodeFromString(actionModelString)
            }
            actionModel?.let {
                actionFactory.create(it).invoke()
            }

            val pushClickEvent = Event(
                type = EventType.INTERNAL,
                name = "push:click",
                attributes = mapOf(
                    "origin" to "main",
//                    "sid" to TODO: get rid of u parameter send the sid somewhere else
                )
            )
            eventClient.registerEvent(pushClickEvent)

            CoroutineScope(Dispatchers.Main).launch {
                withCompletionHandler()
            }
        }
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        openSettingsForNotification: UNNotification?
    ) {
        userNotificationCenterDelegate?.let {
            CoroutineScope(Dispatchers.Main).launch {
                it.userNotificationCenter(center, openSettingsForNotification)
            }
        }
    }
}
