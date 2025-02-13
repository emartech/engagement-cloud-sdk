package com.emarsys.mobileengage.push.extension

import com.emarsys.api.push.BasicPushUserInfo
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSJSONWritingPrettyPrinted
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun Map<String, Any>.toBasicPushUserInfo(json: Json): BasicPushUserInfo {
    val userInfoString = NSString.create(
        NSJSONSerialization.dataWithJSONObject(
            this,
            NSJSONWritingPrettyPrinted,
            null
        )!!, NSUTF8StringEncoding
    ).toString()
    return json.decodeFromString<BasicPushUserInfo>(userInfoString)
}