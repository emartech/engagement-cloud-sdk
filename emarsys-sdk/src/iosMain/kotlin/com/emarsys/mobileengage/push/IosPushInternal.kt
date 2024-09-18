package com.emarsys.mobileengage.push

import com.emarsys.api.AppEvent
import com.emarsys.api.SdkState
import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushInternal
import com.emarsys.context.SdkContextApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel
import com.emarsys.networking.clients.push.PushClientApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
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
    pushClient: PushClientApi,
    storage: TypedStorageApi<String?>,
    pushContext: ApiContext<PushCall>,
    sdkContext: SdkContextApi,
    override val notificationEvents: MutableSharedFlow<AppEvent>,
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val json: Json,
    private val sdkDispatcher: CoroutineDispatcher
) : PushInternal(pushClient, storage, pushContext, notificationEvents), IosPushInstance {
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

    @OptIn(ExperimentalForeignApi::class)
    fun didReceiveNotificationResponse(
        actionIdentifier: String,
        userInfo: Map<String, Any>,
        withCompletionHandler: () -> Unit
    ) {
        CoroutineScope(sdkDispatcher).launch {
            val userInfoData = platform.Foundation.NSJSONSerialization.dataWithJSONObject(
                userInfo,
                NSJSONWritingPrettyPrinted,
                null
            )
            val userInfoJson = NSString.create(userInfoData!!, NSUTF8StringEncoding).toString()
            val pushUserInfo: PushUserInfo = json.decodeFromString(userInfoJson)

            handleActions(actionIdentifier, pushUserInfo)

            withContext(Dispatchers.Main) {
                withCompletionHandler()
            }
        }
    }

    private suspend fun handleActions(
        actionIdentifier: String,
        pushUserInfo: PushUserInfo
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
            actionFactory.create(it).invoke()
        }
    }

    private fun extractDefaultAction(
        pushUserInfo: PushUserInfo,
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
