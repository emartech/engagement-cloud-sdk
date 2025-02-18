package com.emarsys.mobileengage.action.actions

import com.emarsys.mobileengage.action.models.CustomEventActionModel
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class CustomEventAction(
    private val action: CustomEventActionModel,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        sdkEventFlow.emit(
            SdkEvent.External.Custom(
                action.name,
                buildJsonObject {
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
