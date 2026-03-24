package com.sap.ec.mobileengage.push.extension

import com.sap.ec.api.event.model.EventSource
import com.sap.ec.api.push.PushUserInfo
import com.sap.ec.api.push.SilentPushUserInfo
import com.sap.ec.mobileengage.action.models.BasicAppEventActionModel
import com.sap.ec.mobileengage.action.models.addAppEventSource
import com.sap.ec.mobileengage.action.models.addSource
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSJSONWritingPrettyPrinted
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun Map<String, Any>.toPushUserInfo(json: Json): PushUserInfo? {
    return NSJSONSerialization.dataWithJSONObject(this, NSJSONWritingPrettyPrinted, null)
        ?.let { data ->
            NSString.create(data, NSUTF8StringEncoding)?.let { jsonString ->
                val userInfo = json.decodeFromString<PushUserInfo>(jsonString.toString())
                userInfo.copy(
                    notification = userInfo.notification.copy(
                        actions = userInfo.notification.actions?.addAppEventSource(EventSource.Push),
                        defaultAction = userInfo.notification.defaultAction?.let { action ->
                            when (action) {
                                is BasicAppEventActionModel -> action.addSource(EventSource.Push)
                                else -> action
                            }
                        }
                    )
                )
            }
        }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun Map<String, Any>.toSilentPushUserInfo(json: Json): SilentPushUserInfo? {
    return NSJSONSerialization.dataWithJSONObject(this, NSJSONWritingPrettyPrinted, null)
        ?.let { data ->
            NSString.create(data, NSUTF8StringEncoding)?.let { jsonString ->
                val userInfo = json.decodeFromString<SilentPushUserInfo>(jsonString.toString())
                userInfo.copy(
                    notification = userInfo.notification.copy(
                        actions = userInfo.notification.actions?.addAppEventSource(EventSource.Push),
                        defaultAction = userInfo.notification.defaultAction?.let { action ->
                            when (action) {
                                is BasicAppEventActionModel -> action.addSource(EventSource.Push)
                                else -> action
                            }
                        }
                    )
                )
            }
        }
}