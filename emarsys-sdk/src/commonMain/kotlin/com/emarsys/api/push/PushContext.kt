package com.emarsys.api.push

import com.emarsys.api.contact.ContactCall
import com.emarsys.api.generic.ApiContext

class PushContext : ApiContext<PushCall> {
    override val calls = mutableListOf<PushCall>()
}

sealed interface PushCall {
    data class SetPushToken(val pushToken: String) : PushCall
    class ClearPushToken() : PushCall {
        override fun equals(other: Any?): Boolean {
            return other is ClearPushToken
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }
}