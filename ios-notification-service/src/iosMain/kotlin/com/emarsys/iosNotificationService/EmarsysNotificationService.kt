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

@BetaInteropApi
class EmarsysNotificationService(
    private val notificationCenter: NotificationCenterApi = NotificationCenter()
) {

    private val mutex = Mutex()
    private val json = Json {
        encodeDefaults = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    private lateinit var contentHandler: (UNNotificationContent) -> Unit
    private lateinit var bestAttemptContent: UNMutableNotificationContent

    private val uuidProvider: UUIDProvider by lazy { UUIDProvider() }
    private val sessionProvider: SessionProvider by lazy { SessionProvider() }
    private val fileSmith: FileSmith by lazy { FileSmith(uuidProvider) }
    private val downloader: Downloader by lazy { Downloader(sessionProvider.provide(), fileSmith) }

    fun didReceiveNotificationRequest(
        request: UNNotificationRequest,
        withContentHandler: (UNNotificationContent) -> Unit
    ) {
        runBlocking {
            withContext(Dispatchers.Default) {
                contentHandler = withContentHandler
                bestAttemptContent =
                    request.content.mutableCopy() as UNMutableNotificationContent

                val userInfo = bestAttemptContent.userInfo as Map<String, Any>
                val actions = async { createActions(userInfo) }
                val attachments = async { createAttachments(userInfo) }
                val inApp = async { createInApp(userInfo) }

                awaitAll(actions, attachments, inApp)

                contentHandler(bestAttemptContent)
            }
        }
    }

    fun serviceExtensionTimeWillExpire() {
        contentHandler(bestAttemptContent)
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun createActions(userInfo: Map<String, Any>) {
        val ems = userInfo["ems"] as? Map<String, Any> ?: return
        val actions = ems["actions"] as? List<Map<String, Any>> ?: return
        val data = NSJSONSerialization.dataWithJSONObject(
            actions,
            NSJSONWritingPrettyPrinted,
            null
        )
        val actionsJson = NSString.create(data!!, NSUTF8StringEncoding)!!.toString()
        val actionModels: List<ActionModel> = json.decodeFromString(actionsJson)

        val notificationActions = actionModels.map {
            val options =
                if (it is DismissActionModel) UNNotificationActionOptionDestructive else UNNotificationActionOptionForeground
            UNNotificationAction.actionWithIdentifier(it.id, it.title, options)
        }

        val category = UNNotificationCategory.categoryWithIdentifier(
            uuidProvider.provide().UUIDString(),
            notificationActions,
            emptyList<String>(),
            UNNotificationCategoryOptionNone
        )

        notificationCenter.addCategory(category)

        mutex.withLock {
            bestAttemptContent.setCategoryIdentifier(category.identifier)
        }

    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun createAttachments(userInfo: Map<String, Any>) {
        val mediaUrlString = userInfo["image_url"] as String?
        if (!mediaUrlString.isNullOrEmpty()) {
            val mediaUrl = downloader.downloadFile(NSURL(string = mediaUrlString))

            mediaUrl?.let {
                val attachment = UNNotificationAttachment.attachmentWithIdentifier(
                    it.lastPathComponent!!,
                    it,
                    null,
                    null
                )
                mutex.withLock {
                    bestAttemptContent.setAttachments(listOf(attachment))
                }
            }
        }
    }

    private suspend fun createInApp(userInfo: Map<String, Any>) {
        val ems = userInfo["ems"] as? Map<String, Any> ?: return
        val inApp = ems["inapp"] as? Map<String, Any> ?: return
        val inAppUrlString = inApp["url"] as? String ?: return

        val inAppData = downloader.downloadData(NSURL(string = inAppUrlString)) ?: return

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
