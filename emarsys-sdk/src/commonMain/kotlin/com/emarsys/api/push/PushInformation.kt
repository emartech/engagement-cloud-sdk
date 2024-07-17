package com.emarsys.api.push

enum class PushType {
    SilentPush,
    Push
}

data class PushInformation(
    val campaignId: String,
    val pushType: PushType)