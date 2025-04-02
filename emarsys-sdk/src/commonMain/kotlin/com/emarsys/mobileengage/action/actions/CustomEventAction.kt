package com.emarsys.mobileengage.action.actions

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.mobileengage.action.models.CustomEventActionModel
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class CustomEventAction(
    private val action: CustomEventActionModel,
    private val sdkEventDistributor: SdkEventDistributorApi
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        sdkEventDistributor.registerAndStoreEvent(
            SdkEvent.External.Custom(
                name = action.name,
                attributes = buildJsonObject {
                    action.payload?.forEach { (key, value) ->
                        put(
                            key,
                            JsonPrimitive(value)
                        )
                    }
                })
        )
    }
}
