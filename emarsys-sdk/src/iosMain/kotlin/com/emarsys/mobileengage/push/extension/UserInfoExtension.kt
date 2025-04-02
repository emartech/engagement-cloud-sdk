package com.emarsys.mobileengage.push.extension

import com.emarsys.api.push.BasicPushUserInfo
import com.emarsys.api.push.BasicPushUserInfoEms
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSJSONWritingPrettyPrinted
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun Map<String, Any>.toBasicPushUserInfo(json: Json): BasicPushUserInfo {
    val emsJson = extractJsonObject(this, "ems", json)
    return if (emsJson != null && emsJson.keys.contains("version")) {
        val notificationJson = extractJsonObject(this, "notification", json)
        val treatments: JsonObject? = emsJson["treatments"]?.jsonObject
        BasicPushUserInfo(
            BasicPushUserInfoEms(
                multichannelId = emsJson.getValue("campaignId").jsonPrimitive.content,
                inapp = notificationJson?.get("inapp")?.jsonObject?.let {
                    json.decodeFromJsonElement(it)
                },
                sid = treatments?.get("sid")?.jsonPrimitive?.contentOrNull,
                actions = notificationJson?.get("actions")?.jsonArray?.let {
                    json.decodeFromJsonElement(it)
                },
                badgeCount = notificationJson?.get("badgeCount")?.jsonObject?.let {
                    json.decodeFromJsonElement(it)
                }
            )
        )
    } else {
        val userInfoString = NSString.create(
            NSJSONSerialization.dataWithJSONObject(
                this,
                NSJSONWritingPrettyPrinted,
                null
            )!!, NSUTF8StringEncoding
        ).toString()
        json.decodeFromString<BasicPushUserInfo>(userInfoString)
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun extractJsonObject(userInfo: Map<String, Any>, key: String, json: Json): JsonObject? {
    return userInfo[key]?.let { extractedMap ->
        NSJSONSerialization.dataWithJSONObject(extractedMap, NSJSONWritingPrettyPrinted, null)
            ?.let { data ->
                NSString.create(data, NSUTF8StringEncoding)?.let { jsonString ->
                    json.decodeFromString(jsonString.toString())
                }
            }
    }
}
