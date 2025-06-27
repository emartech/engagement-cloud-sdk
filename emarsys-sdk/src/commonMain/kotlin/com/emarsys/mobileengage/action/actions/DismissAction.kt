package com.emarsys.mobileengage.action.actions

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.models.DismissActionModel

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
