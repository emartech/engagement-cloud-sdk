package com.sap.ec.mobileengage.embeddedmessaging.models

import com.sap.ec.InternalSdkApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@InternalSdkApi
@Serializable
data class MessageTagUpdate(
    val messageId: String,
    val operation: TagOperation,
    val tag: String,
    val trackingInfo: String
)

@InternalSdkApi
@Serializable
enum class TagOperation {
    @SerialName("add") Add,
    @SerialName("remove") Remove
}