package com.sap.ec.mobileengage.action.actions

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.action.models.DismissActionModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class DismissAction(
    private val action: DismissActionModel,
    private val sdkEventDistributor: SdkEventDistributorApi
): Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        action.dismissId?.let {
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.Dismiss(it)
            )
        }
    }
}
