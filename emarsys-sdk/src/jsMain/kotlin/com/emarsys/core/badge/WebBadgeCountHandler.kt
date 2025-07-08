package com.emarsys.core.badge

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import web.broadcast.BroadcastChannel
import web.events.EventHandler
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class WebBadgeCountHandler(
    private val onBadgeCountUpdateReceivedBroadcastChannel: BroadcastChannel,
    private val sdkEventDistributor: SdkEventDistributorApi,
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
            sdkEventDistributor.registerEvent(
                SdkEvent.External.Api.BadgeCountEvent(
                    name = "badgeCount",
                    badgeCount = badgeCount.value,
                    method = badgeCount.method.name,
                )
            )
        } catch (e: Exception) {
            sdkLogger.error("WebBadgeCountHandler - handleBadgeCount", e)
        }
    }
}