package com.emarsys.iosNotificationService

import com.emarsys.iosNotificationService.file.FileSmith
import com.emarsys.iosNotificationService.models.ActionModel
import com.emarsys.iosNotificationService.models.DismissActionModel
import com.emarsys.iosNotificationService.networking.Downloader
import com.emarsys.iosNotificationService.notification.NotificationCenter
import com.emarsys.iosNotificationService.notification.NotificationCenterApi
import com.emarsys.iosNotificationService.provider.SessionProvider
import com.emarsys.iosNotificationService.provider.UUIDProvider
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSJSONWritingPrettyPrinted
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.lastPathComponent
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAction
import platform.UserNotifications.UNNotificationActionOptionDestructive
import platform.UserNotifications.UNNotificationActionOptionForeground
import platform.UserNotifications.UNNotificationAttachment
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptionNone
import platform.UserNotifications.UNNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationServiceExtension

@BetaInteropApi
class NotificationService(private val notificationCenter: NotificationCenterApi = NotificationCenter()) : UNNotificationServiceExtension() {

    private val mutex = Mutex()

    private lateinit var contentHandler: (UNNotificationContent?) -> Unit
    private lateinit var bestAttemptContent: UNMutableNotificationContent

    private val uuidProvider: UUIDProvider by lazy { UUIDProvider() }
    private val sessionProvider: SessionProvider by lazy { SessionProvider() }
    private val fileSmith: FileSmith by lazy { FileSmith(uuidProvider) }
    private val downloader: Downloader by lazy { Downloader(sessionProvider.provide(), fileSmith) }

    override fun didReceiveNotificationRequest(
        request: UNNotificationRequest,
        withContentHandler: (UNNotificationContent?) -> Unit
    ) {
        contentHandler = withContentHandler
        bestAttemptContent = request.content.mutableCopy() as UNMutableNotificationContent

        runBlocking {
            val userInfo = bestAttemptContent.userInfo as Map<String, Any>

            val actions = createActions(userInfo)
            val attachments = createAttachments(userInfo)
            val inApp = createInApp(userInfo)

            awaitAll(actions, attachments, inApp)
        }

        contentHandler(bestAttemptContent)
    }

    override fun serviceExtensionTimeWillExpire() {
        contentHandler(bestAttemptContent)
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun createActions(userInfo: Map<String, Any>): Deferred<Unit> = withContext(Dispatchers.Default) {
        async {
            val ems = userInfo["ems"] as? Map<String, Any> ?: return@async
            val actions = ems["actions"] as? List<Map<String, Any>> ?: return@async
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

            mutex.withLock {
                bestAttemptContent.setCategoryIdentifier(category.identifier)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun createAttachments(userInfo: Map<String, Any>) = withContext(Dispatchers.Default) {
        async {
            val mediaUrlString = userInfo["image_url"] as? String ?: return@async
            val mediaUrl = downloader.downloadFile(NSURL(string = mediaUrlString))
            val attachment = UNNotificationAttachment.attachmentWithIdentifier(
                mediaUrl!!.lastPathComponent!!,
                mediaUrl,
                null,
                null
            )
            mutex.withLock {
                bestAttemptContent.setAttachments(listOf(attachment))
            }
        }
    }

    private suspend fun createInApp(userInfo: Map<String, Any>) = withContext(Dispatchers.Default) {
        async {
            val ems = userInfo["ems"] as? Map<String, Any> ?: return@async
            val inApp = ems["inapp"] as? Map<String, Any> ?: return@async
            val inAppUrlString = inApp["url"] as? String ?: return@async

            val inAppData = downloader.downloadData(NSURL(string = inAppUrlString)) ?: return@async

            val mutableInApp = inApp.toMutableMap()
            val mutableEms = ems.toMutableMap()
            val mutableUserInfo = userInfo.toMutableMap()

            mutableInApp["inAppData"] = inAppData
            mutableEms["inapp"] = mutableInApp
            mutableUserInfo["ems"] = mutableEms

            mutex.withLock {
                bestAttemptContent.setUserInfo(mutableUserInfo.toMap())
            }
        }
    }

}
