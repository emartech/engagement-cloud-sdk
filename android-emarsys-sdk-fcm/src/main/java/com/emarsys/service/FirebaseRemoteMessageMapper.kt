package com.emarsys.service

import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

object FirebaseRemoteMessageMapper {
    fun map(remoteMessage: RemoteMessage): JSONObject {
        val json = JSONObject()
        with(remoteMessage.data) {
            this["notification.title"]?.let {
                json.put("title", it)
            }

            this["notification.body"]?.let {
                json.put("body", it)
            }

            this["notification.icon"]?.let {
                json.put("iconUrlString", it)
            }

            this["notification.image"]?.let {
                json.put("imageUrlString", it)
            }

            val pushData = JSONObject()

            this["ems.silent"]?.let {
                pushData.put("silent", it)
            }

            this["ems.sid"]?.let {
                pushData.put("sid", it)
            }

            val defaultAction = extractDefaultAction(this)
            pushData.put("defaultAction", defaultAction)

            this["ems.actions"]?.let {
                pushData.put("sid", it)
            }

            this["ems.inapp"]?.let {
                pushData.put("inApp", it)
            }

            this["ems.root_params"]?.let {
                pushData.put("rootParams", it)
            }

            val platformContext = JSONObject()

            this["notification.channel_id"]?.let {
                platformContext.put("channelId", it)
            }

            this["ems.version"]?.let {
                platformContext.put("fcmVersion", it)
            }
            this["ems.multichannel_id"]?.let {
                platformContext.put("mutliChannelId", it)
            }

            pushData.put("platformContext", platformContext)

            json.put("data", pushData)
        }

        return json
    }

    private fun extractDefaultAction(remoteMessageData: Map<String, String?>): String? {
        val name = remoteMessageData.getOrDefault("ems.tap_actions.default_action.name", null)
        val type = remoteMessageData.getOrDefault("ems.tap_actions.default_action.type", null)
        val url = remoteMessageData.getOrDefault("ems.tap_actions.default_action.url", null)
        val payload =
            remoteMessageData.getOrDefault("ems.tap_actions.default_action.payload", null)
        val payloadObject = payload?.let { JSONObject(payload) }

        val defaultAction = if (type != null) {
            JSONObject()
                .put("name", name)
                .put("type", type)
                .put("url", url)
                .put("payload", payloadObject)
        } else null

        return defaultAction?.toString()
    }
}
