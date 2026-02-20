package com.sap.ec.mobileengage.action.models

import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator


sealed interface ActionModel

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class BasicActionModel : ActionModel {
    abstract val reporting: String
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class PresentableActionModel : ActionModel {
    abstract val id: String
    abstract val reporting: String
    abstract val title: String
}

fun BasicActionModel.amendForJsBridge(data: InAppJsBridgeData): BasicActionModel {
    if (this is DismissActionModel) {
        this.dismissId = data.dismissId
    } else if (this is BasicInAppButtonClickedActionModel) {
        this.trackingInfo = data.trackingInfo
    }

    return this
}