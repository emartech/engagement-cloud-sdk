package com.emarsys.service

import org.json.JSONObject

object HmsRemoteMessageMapper {
    private const val MISSING_MESSAGE_ID = "Missing messageId"
    private const val EMPTY_JSON = "{}"
    private const val MISSING_SID = "Missing sid"

    fun map(remoteMessageContent: Map<String, String>): JSONObject {
        val messageContentCopy = remoteMessageContent.toMutableMap()
        val remoteMessagePayload = JSONObject()

        val messageId = messageContentCopy.remove("message_id") ?: MISSING_MESSAGE_ID
        val title = messageContentCopy.remove("title")
        val body = messageContentCopy.remove("body")
        val iconUrlString = messageContentCopy.remove("icon_url")
        val imageUrlString = messageContentCopy.remove("image_url")
        val channelId = messageContentCopy.remove("channel_id")

        val u = messageContentCopy.remove("u") ?: EMPTY_JSON
        val sid = JSONObject(u).getNullableString("sid") ?: MISSING_SID

        val emsPayload = messageContentCopy.remove("ems")?.let { JSONObject(it) } ?: JSONObject()
        val silent = emsPayload.optBoolean("silent", false)
        val campaignId = emsPayload.optString("multichannelId")
        val defaultAction = emsPayload.getNullableString("default_action")
        val actions = emsPayload.getNullableString("actions")
        val inapp = emsPayload.getNullableString("inapp")
        val style = emsPayload.getNullableString("style")
        val notificationMethod = emsPayload.getNullableString("notificationMethod")

        val platformContext = JSONObject()
            .put("channelId", channelId)

        style?.let { platformContext.put("style", it) }
        notificationMethod?.let { platformContext.put("notificationMethod", it) }

        val data = JSONObject()
            .put("silent", silent)
            .put("sid", sid)
            .put("campaignId", campaignId)
            .put("platformContext", platformContext)
            .put("rootParams", messageContentCopy)
            .put("u", u)

         defaultAction?.let { data.put("defaultAction", it)}
         actions?.let { data.put("actions", it)}
         inapp?.let { data.put("inapp", it)}

        remoteMessagePayload
            .put("messageId", messageId)
            .put("title", title)
            .put("body", body)
            .put("iconUrlString", iconUrlString)
            .put("imageUrlString", imageUrlString)
            .put("data", data)

        return remoteMessagePayload
    }
}

fun JSONObject.getNullableString(key: String): String? {
    return if (this.isNull(key)) null else this.getString(key)
}