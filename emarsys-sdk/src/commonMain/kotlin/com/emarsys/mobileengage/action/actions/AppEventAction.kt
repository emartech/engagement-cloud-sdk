package com.emarsys.mobileengage.action.actions

import com.emarsys.mobileengage.action.models.AppEventActionModel
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class AppEventAction(
    private val action: AppEventActionModel,
    private val sdkEvents: MutableSharedFlow<SdkEvent>
) : Action<SdkEvent> {
    //TODO we should add the source!!
    override suspend fun invoke(value: SdkEvent?) {
        value?.let {
            sdkEvents.emit(
                SdkEvent.External.Api.InApp(
                    name = action.name,
                    attributes = buildJsonObject {
                        action.payload?.forEach { (key, value) ->
                            put(
                                key,
                                JsonPrimitive(value)
                            )
                        }
                    },
                )
            )
        }
    }
}