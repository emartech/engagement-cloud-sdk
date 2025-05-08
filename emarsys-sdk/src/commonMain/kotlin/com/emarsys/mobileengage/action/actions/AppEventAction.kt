package com.emarsys.mobileengage.action.actions

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.mobileengage.action.models.AppEventActionModel
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class AppEventAction(
    private val action: AppEventActionModel,
    private val sdkEventDistributor: SdkEventDistributorApi
) : Action<SdkEvent> {
    //TODO we should add the source!!
    override suspend fun invoke(value: SdkEvent?) {
        value?.let {
            sdkEventDistributor.registerEvent(
                SdkEvent.External.Api.AppEvent(
                    name = action.name,
                    attributes = buildJsonObject {
                        action.payload?.forEach { (key, value) ->
                            put(key, value)
                        }
                    },
                )
            )
        }
    }
}