package com.emarsys.core.badge

import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.events.SdkEvent
import com.emarsys.mobileengage.events.SdkEventSource
import com.emarsys.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import web.broadcast.BroadcastChannel
import web.events.EventHandler

class WebBadgeCountHandler(
    private val onBadgeCountUpdateReceivedBroadcastChannel: BroadcastChannel,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val coroutineScope: CoroutineScope,
    private val sdkLogger: Logger
) : WebBadgeCountHandlerApi {

    override suspend fun register() {
        onBadgeCountUpdateReceivedBroadcastChannel.onmessage = EventHandler { event ->
            coroutineScope.launch {
                handleBadgeCount(event.data.unsafeCast<String>())
            }
        }
    }

    internal suspend fun handleBadgeCount(badgeCountString: String) {
        try {
            val badgeCount =
                JsonUtil.json.decodeFromString<BadgeCount>(badgeCountString)
            sdkEventFlow.emit(
                SdkEvent(
                    SdkEventSource.BadgeCount,
                    badgeCount.method.name,
                    mapOf("badgeCount" to badgeCount.value)
                )
            )
        } catch (e: Exception) {
            sdkLogger.error("WebBadgeCountHandler - handleBadgeCount", e)
        }
    }
}