package com.emarsys.networking.clients.logging

import com.emarsys.core.device.DeviceInfoForLogs
import com.emarsys.core.log.LogLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LogRequest(
    val deviceInfo: DeviceInfoForLogs,
    val level: LogLevel,
    val message: String?,
    val type: String,
    val url:String?,
    val statusCode:String?,
    val networkingDuration:String?,
    val networkingEnd:String?,
    val networkingStart:String?,
    val inDbDuration:String?,
    val inDbEnd:String?,
    val inDbStart:String?,
    val loadingTimeDuration:String?,
    val loadingTimeEnd:String?,
    val loadingTimeStart:String?,
    val onScreenDuration:String?,
    val onScreenEnd:String?,
    val onScreenStart:String?,
    val exception:String?,
    val reason:String?,
    val stackTrace:String?,
    val breadcrumbs: JsonObject?,
)