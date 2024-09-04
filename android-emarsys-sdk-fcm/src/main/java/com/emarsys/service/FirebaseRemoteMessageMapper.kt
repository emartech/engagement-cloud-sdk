package com.emarsys.service

import com.emarsys.service.model.NotificationOperation
import com.emarsys.service.provider.UuidStringProvider
import org.json.JSONArray
import org.json.JSONObject

object FirebaseRemoteMessageMapper {
    private val uuidStringProvider = UuidStringProvider()

    private const val MISSING_MESSAGE_ID = "Missing messageId"
    private const val MISSING_SID = "Missing sid"

    fun map(remoteMessageContent: Map<String, String>): JSONObject {
        val messageContentCopy = remoteMessageContent.toMutableMap()
        val remoteMessagePayload = JSONObject()

        val messageId = messageContentCopy.remove("ems.message_id") ?: MISSING_MESSAGE_ID
        val title = messageContentCopy.remove("notification.title")
        val body = messageContentCopy.remove("notification.body")
        val iconUrlString = messageContentCopy.remove("notification.icon")
        val imageUrlString = messageContentCopy.remove("notification.image")
        val channelId = messageContentCopy.remove("notification.channel_id")
        val sid = messageContentCopy.remove("ems.sid") ?: MISSING_SID

        val rootParams = mapRootParams(messageContentCopy.remove("ems.root_params"))
        val u = rootParams.optJSONObject("u") ?: JSONObject()

        val silent = messageContentCopy.remove("ems.silent") ?: false
        val campaignId = messageContentCopy.remove("ems.multichannel_id")
        val defaultAction = extractDefaultAction(messageContentCopy)
        val actions = messageContentCopy.remove("ems.actions")?.let { JSONArray(it) }
        val inapp = messageContentCopy.remove("ems.inapp")?.let { JSONObject(it) }
        val style = messageContentCopy.remove("ems.style")
        val notificationMethod = parseNotificationMethod(messageContentCopy)

        val platformData = JSONObject()
            .put("channelId", channelId)
            .put("notificationMethod", notificationMethod)

        style?.let { platformData.put("style", it) }

        val data = JSONObject()
            .put("silent", silent)
            .put("sid", sid)
            .put("campaignId", campaignId)
            .put("platformData", platformData)
            .put("rootParams", rootParams)
            .put("u", u)

        defaultAction?.let { data.put("defaultTapAction", it) }
        actions?.let { data.put("actions", it) }
        inapp?.let { data.put("pushToInApp", it) }

        remoteMessagePayload
            .put("messageId", messageId)
            .put("title", title)
            .put("body", body)
            .put("iconUrlString", iconUrlString)
            .put("imageUrlString", imageUrlString)
            .put("data", data)

        return remoteMessagePayload
    }

    private fun mapRootParams(rootParams: String?): JSONObject {
        return rootParams?.let {
            try {
                JSONObject(it)
            } catch (ignored: Exception) {
                JSONObject()
            }
        } ?: JSONObject()
    }

    private fun extractDefaultAction(remoteMessageData: Map<String, String?>): JSONObject? {
        val name = remoteMessageData.getOrDefault("ems.tap_actions.default_action.name", null)
        val type = remoteMessageData.getOrDefault("ems.tap_actions.default_action.type", null)
        val url = remoteMessageData.getOrDefault("ems.tap_actions.default_action.url", null)
        val payload =
            remoteMessageData.getOrDefault("ems.tap_actions.default_action.payload", null)
        val payloadObject = payload?.let { JSONObject(payload) }

        val defaultAction = if (type != null) {
            val action = JSONObject().put("type", type)

            name?.let { action.put("name", it) }
            url?.let { action.put("url", it) }

            if (!payloadObject.isEmptyOrNull()) {
                action.put("payload", payloadObject)
            }
            action
        } else null

        return defaultAction
    }

    private fun parseNotificationMethod(remoteMessageData: Map<String, String?>): JSONObject {
        val collapseId =
            remoteMessageData["ems.notification_method.collapse_key"] ?: uuidStringProvider.provide()
        val operation = remoteMessageData["ems.notification_method.operation"]?.uppercase()
            ?: NotificationOperation.INIT.name.uppercase()

        return JSONObject()
            .put("collapseId", collapseId)
            .put("operation", operation)
    }
}

fun JSONObject?.isEmptyOrNull(): Boolean {
    return this == null || this.length() == 0
}
