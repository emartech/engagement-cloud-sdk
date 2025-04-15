package com.emarsys.mobileengage.action.actions

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.mobileengage.action.models.AppEventActionModel
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class AppEventAction(
    private val action: AppEventActionModel,
    private val sdkEventDistributor: SdkEventDistributorApi
) : Action<SdkEvent> {
    //TODO we should add the source!!
    override suspend fun invoke(value: SdkEvent?) {
        value?.let {
            sdkEventDistributor.registerEvent(
                SdkEvent.External.Api.InApp(
                    name = action.name,
                    attributes = buildJsonObject {
                        action.payload?.forEach { (key, value) ->
                            put(
                                key,
                                JsonPrimitive(value)
                            )
                        }
                    },
                )
            )
        }
    }
}