package com.sap.ec.mobileengage.action.actions

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.action.models.CustomEventActionModel
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
