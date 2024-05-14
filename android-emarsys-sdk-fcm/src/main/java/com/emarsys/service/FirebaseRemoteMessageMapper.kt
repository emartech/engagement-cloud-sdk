package com.emarsys.service

import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

object FirebaseRemoteMessageMapper {
    fun map(remoteMessage: RemoteMessage): JSONObject {
        //TODO: this mapper needs to be adjusted to the new version of FCM payload
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

            this["ems.inapp"]?.let {
                pushData.put("inApp", it)
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
}
