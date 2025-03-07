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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSJSONWritingPrettyPrinted
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAction
import platform.UserNotifications.UNNotificationActionOptionDestructive
import platform.UserNotifications.UNNotificationActionOptionForeground
import platform.UserNotifications.UNNotificationAttachment
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptionNone
import platform.UserNotifications.UNNotificationContent
import platform.UserNotifications.UNNotificationRequest

enum class PayloadVersion {
    V1,
    V2
}

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
    private var payloadVersion: PayloadVersion? = null
    private var emsJson: JsonObject? = null
    private var notificationJson: JsonObject? = null

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
                emsJson = extractJsonObject(userInfo, "ems")
                notificationJson = extractJsonObject(userInfo, "notification")
                payloadVersion = if (emsJson != null && emsJson!!.keys.contains("version")) { PayloadVersion.V2 } else { PayloadVersion.V1 }
                val actions = async { createActions() }
                val attachments = async { createAttachments(userInfo) }

                awaitAll(actions, attachments)

                contentHandler(bestAttemptContent)
            }
        }
    }

    fun serviceExtensionTimeWillExpire() {
        contentHandler(bestAttemptContent)
    }

    private suspend fun createActions() {
        actionModels()?.let { actionModels ->
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
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun createAttachments(userInfo: Map<String, Any>) {
        imageUrl(userInfo)?.let { imageUrl ->
            val mediaUrl = downloader.downloadFile(imageUrl)

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

    private fun actionModels(): List<ActionModel>? {
        return if (payloadVersion == PayloadVersion.V1) {
            emsJson?.get("actions")?.jsonArray?.let {
                json.decodeFromJsonElement(it)
            }
        } else {
            notificationJson?.get("actions")?.jsonArray?.let {
                json.decodeFromJsonElement(it)
            }
        }
    }

    private fun imageUrl(userInfo: Map<String, Any>): NSURL? {
        return if (payloadVersion == PayloadVersion.V1) {
            (userInfo["image_url"] as String?)?.let {
                if (it.isNotBlank()) {
                    NSURL(string = it)
                } else null
            }
        } else {
            notificationJson?.get("imageUrl")?.jsonPrimitive?.contentOrNull?.let {
                if (it.isNotBlank()) {
                    NSURL(string = it)
                } else null
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun extractJsonObject(userInfo: Map<String, Any>, key: String): JsonObject? {
        return userInfo[key]?.let { extractedMap ->
            NSJSONSerialization.dataWithJSONObject(extractedMap, NSJSONWritingPrettyPrinted, null)?.let { data ->
                NSString.create(data, NSUTF8StringEncoding)?.let { jsonString ->
                    json.decodeFromString(jsonString.toString())
                }
            }
        }
    }
}
