package com.sap.ec.mobileengage.action.actions

import com.sap.ec.api.event.model.AppEvent
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.action.models.AppEventActionModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class AppEventAction(
    private val action: AppEventActionModel,
    private val sdkEventDistributor: SdkEventDistributorApi
) : Action<SdkEvent> {
    override suspend fun invoke(value: SdkEvent?) {
        sdkEventDistributor.registerPublicEvent(
            AppEvent(
                name = action.name,
                payload = action.payload,
                source = action.source
            )
        )
    }
}