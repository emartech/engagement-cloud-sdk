package com.sap.ec.mobileengage.action.actions

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.action.models.AppEventActionModel
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class AppEventAction(
    private val action: AppEventActionModel,
    private val sdkEventDistributor: SdkEventDistributorApi
) : Action<SdkEvent> {
    //TODO we should add the source!!
    override suspend fun invoke(value: SdkEvent?) {
        sdkEventDistributor.registerEvent(
            SdkEvent.External.Api.AppEvent(
                name = action.name,
                attributes = action.payload?.let {
                    buildJsonObject {
                        it.forEach { (key, value) ->
                            put(key, value)
                        }
                    }
                }
            )
        )
    }
}