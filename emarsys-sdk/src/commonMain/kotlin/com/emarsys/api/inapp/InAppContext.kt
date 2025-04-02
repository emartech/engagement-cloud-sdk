package com.emarsys.api.inapp

import kotlinx.serialization.Serializable

internal class InAppContext(override val calls: MutableList<InAppCall>) : InAppContextApi

@Serializable
sealed interface InAppCall {

    @Serializable
    class Pause : InAppCall {
        override fun equals(other: Any?): Boolean {
            return other is Pause
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    @Serializable
    class Resume : InAppCall {
        override fun equals(other: Any?): Boolean {
            return other is Resume
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }
}