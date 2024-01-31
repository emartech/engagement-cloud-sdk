package com.emarsys.api.push

import com.emarsys.api.generic.ApiContext
import kotlinx.serialization.Serializable

class PushContext(override val calls: MutableList<PushCall>) : ApiContext<PushCall>

@Serializable
sealed interface PushCall {

    @Serializable
    data class RegisterPushToken(val pushToken: String) : PushCall

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