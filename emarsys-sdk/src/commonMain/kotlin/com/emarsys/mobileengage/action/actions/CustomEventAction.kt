package com.emarsys.mobileengage.action.actions

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.models.CustomEventActionModel
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal class CustomEventAction(
    private val action: CustomEventActionModel,
    private val sdkEventDistributor: SdkEventDistributorApi
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        sdkEventDistributor.registerEvent(
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
        )?.await()
    }
}
