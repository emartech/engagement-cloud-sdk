package com.emarsys.api.push

import kotlinx.serialization.Serializable

internal class PushContext(override val calls: MutableList<PushCall>) : PushContextApi

@Serializable
sealed interface PushCall {

    @Serializable
    data class RegisterPushToken(val pushToken: String) : PushCall

    @Serializable
    data class HandleSilentMessageWithUserInfo(val userInfo: SilentPushUserInfo) : PushCall

    @Serializable
    data class ClearPushToken(val applicationCode: String?) : PushCall
}