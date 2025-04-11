package com.emarsys.api.push

import kotlinx.serialization.Serializable

internal class PushContext(override val calls: MutableList<PushCall>) : PushContextApi

@Serializable
sealed interface PushCall {

    @Serializable
    data class RegisterPushToken(val pushToken: String) : PushCall

    @Serializable
    data class HandleSilentMessageWithUserInfo(val userInfo: PushUserInfo) : PushCall

    @Serializable
    class ClearPushToken : PushCall {
        override fun equals(other: Any?): Boolean {
            return other is ClearPushToken
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }
}