package com.emarsys.mobileengage.action.actions

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.networking.model.Response
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.models.CustomEventActionModel
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class CustomEventAction(
    private val action: CustomEventActionModel,
    private val sdkEventDistributor: SdkEventDistributorApi
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        sdkEventDistributor.registerEvent(
            SdkEvent.External.Custom(
                name = action.name,
                attributes = action.payload?.let {
                    buildJsonObject {
                        it.forEach { (key, value) ->
                            put(
                                key,
                                JsonPrimitive(value)
                            )
                        }
                    }
                })
        ).await<SdkEvent.Internal.Sdk.Answer.Response<Response>>()
    }
}
