package com.emarsys.core.badge

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import web.broadcast.BroadcastChannel
import web.events.EventHandler

class WebBadgeCountHandler(
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
            sdkEventDistributor.registerAndStoreEvent(
                SdkEvent.External.Api.BadgeCount(
                    name = badgeCount.method.name,
                    attributes = buildJsonObject {
                        put(
                            "badgeCount",
                            JsonPrimitive(badgeCount.value)
                        )
                    }
                )
            )
        } catch (e: Exception) {
            sdkLogger.error("WebBadgeCountHandler - handleBadgeCount", e)
        }
    }
}