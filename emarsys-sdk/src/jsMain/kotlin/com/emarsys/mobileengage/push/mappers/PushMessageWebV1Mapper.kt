package com.emarsys.mobileengage.push.mappers

import com.emarsys.core.log.Logger
import com.emarsys.core.mapper.Mapper
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.push.PushData
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.mobileengage.push.model.v1.RemoteWebPushMessageV1
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PushMessageWebV1Mapper(
    private val json: Json,
    private val logger: Logger
) : Mapper<String, JsPushMessage> {

    override suspend fun map(from: String): JsPushMessage? {
        return try {
            val remoteMessage = json.decodeFromString<RemoteWebPushMessageV1>(from)

            val defaultAction = json.encodeToString(remoteMessage.notification.defaultAction)
            val basicDefaultActionModel: BasicActionModel = json.decodeFromString(defaultAction)

            JsPushMessage(
                remoteMessage.ems.id,
                remoteMessage.notification.title,
                remoteMessage.notification.body,
                remoteMessage.notification.icon,
                remoteMessage.notification.imageUrl,
                PushData(
                    sid = remoteMessage.ems.sid,
                    campaignId = remoteMessage.ems.campaignId,
                    actions = remoteMessage.notification.actions,
                    platformData = JsPlatformData(remoteMessage.ems.applicationCode),
                    pushToInApp = null,
                    defaultTapAction = basicDefaultActionModel
                )
            )
        } catch (exception: Exception) {
            logger.error("WebPushMessageV1Mapper", exception)
            null
        }
    }
}