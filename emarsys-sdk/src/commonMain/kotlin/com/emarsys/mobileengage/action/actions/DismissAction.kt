package com.emarsys.mobileengage.action.actions

import com.emarsys.mobileengage.action.models.DismissActionModel
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow

class DismissAction(
    private val action: DismissActionModel,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>
): Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        action.dismissId?.let {
            sdkEventFlow.emit(
                SdkEvent.Internal.Sdk.Dismiss(it)
            )
        }
    }
}
