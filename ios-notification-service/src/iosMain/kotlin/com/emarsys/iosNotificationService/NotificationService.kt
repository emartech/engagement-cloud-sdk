package com.emarsys.iosNotificationService

import com.emarsys.iosNotificationService.models.ActionModel
import com.emarsys.iosNotificationService.models.DismissActionModel
import com.emarsys.iosNotificationService.notification.NotificationCenter
import com.emarsys.iosNotificationService.notification.NotificationCenterApi
import com.emarsys.iosNotificationService.provider.UUIDProvider
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSJSONWritingPrettyPrinted
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAction
import platform.UserNotifications.UNNotificationActionOptionDestructive
import platform.UserNotifications.UNNotificationActionOptionForeground
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptionNone
import platform.UserNotifications.UNNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationServiceExtension

class NotificationService(private val notificationCenter: NotificationCenterApi = NotificationCenter()) : UNNotificationServiceExtension() {

    private lateinit var contentHandler: (UNNotificationContent?) -> Unit
    private lateinit var bestAttemptContent: UNMutableNotificationContent

    private val uuidProvider: UUIDProvider by lazy { UUIDProvider() }

    override fun didReceiveNotificationRequest(
        request: UNNotificationRequest,
        withContentHandler: (UNNotificationContent?) -> Unit
    ) {
        contentHandler = withContentHandler
        bestAttemptContent = request.content.mutableCopy() as UNMutableNotificationContent

        runBlocking {
            val userInfo = bestAttemptContent.userInfo as Map<String, Any>
            createActions(userInfo)
        }

        contentHandler(bestAttemptContent)
    }

    override fun serviceExtensionTimeWillExpire() {
        contentHandler(bestAttemptContent)
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private suspend fun createActions(userInfo: Map<String, Any>) {
        val actions = (userInfo["ems"] as Map<String, Any>)["actions"] as List<Map<String, Any>>
        val data = NSJSONSerialization.dataWithJSONObject(actions, NSJSONWritingPrettyPrinted, null)
        val actionsJson = NSString.create(data!!, NSUTF8StringEncoding)!!.toString()
        val actionModels: List<ActionModel> = Json.decodeFromString(actionsJson)

        val notificationActions = actionModels.map {
            val options =
                if (it is DismissActionModel) UNNotificationActionOptionDestructive else UNNotificationActionOptionForeground
            UNNotificationAction.actionWithIdentifier(it.id, it.title, options)
        }
        val category = UNNotificationCategory.categoryWithIdentifier(
            uuidProvider.provide().UUIDString(),
            actions,
            notificationActions,
            UNNotificationCategoryOptionNone
        )

        notificationCenter.addCategory(category)

        bestAttemptContent.setCategoryIdentifier(category.identifier)
    }

}

