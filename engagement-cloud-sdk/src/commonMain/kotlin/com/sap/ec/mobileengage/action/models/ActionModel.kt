package com.sap.ec.mobileengage.action.models

import com.sap.ec.InternalSdkApi
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator


//needs to be exposed for ServiceWorker
@InternalSdkApi
sealed interface ActionModel

//needs to be exposed for ServiceWorker
@InternalSdkApi
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class BasicActionModel : ActionModel {
    abstract val reporting: String
}

//needs to be exposed for ServiceWorker
@InternalSdkApi
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class PresentableActionModel : ActionModel {
    abstract val id: String
    abstract val reporting: String
    abstract val title: String
}

internal fun BasicActionModel.amendForJsBridge(data: InAppJsBridgeData): BasicActionModel {
    if (this is DismissActionModel) {
        this.dismissId = data.dismissId
        this.inAppType = data.inAppType

    } else if (this is BasicInAppButtonClickedActionModel) {
        this.trackingInfo = data.trackingInfo
        this.inAppType = data.inAppType

    }

    return this
}