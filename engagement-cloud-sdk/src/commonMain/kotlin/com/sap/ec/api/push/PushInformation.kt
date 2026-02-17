package com.sap.ec.api.push

enum class PushType {
    SilentPush,
    Push
}

data class PushInformation(
    val campaignId: String,
    val pushType: PushType)