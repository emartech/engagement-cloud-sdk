package com.sap.ec.mobileengage.push.extension

import com.sap.ec.api.push.PushUserInfo
import com.sap.ec.api.push.SilentPushUserInfo
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSJSONWritingPrettyPrinted
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun Map<String, Any>.toPushUserInfo(json: Json): PushUserInfo? {
    return NSJSONSerialization.dataWithJSONObject(this, NSJSONWritingPrettyPrinted, null)
        ?.let { data ->
            NSString.create(data, NSUTF8StringEncoding)?.let { jsonString ->
                json.decodeFromString(jsonString.toString())
            }
        }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun Map<String, Any>.toSilentPushUserInfo(json: Json): SilentPushUserInfo? {
    return NSJSONSerialization.dataWithJSONObject(this, NSJSONWritingPrettyPrinted, null)
        ?.let { data ->
            NSString.create(data, NSUTF8StringEncoding)?.let { jsonString ->
                json.decodeFromString(jsonString.toString())
            }
        }
}