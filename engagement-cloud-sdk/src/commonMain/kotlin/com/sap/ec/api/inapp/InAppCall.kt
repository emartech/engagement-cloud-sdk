package com.sap.ec.api.inapp

import kotlinx.serialization.Serializable

@Serializable
internal sealed interface InAppCall {

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