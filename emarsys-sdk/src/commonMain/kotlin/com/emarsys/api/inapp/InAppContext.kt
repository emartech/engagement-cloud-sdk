package com.emarsys.api.inapp

import com.emarsys.api.generic.ApiContext
import kotlinx.serialization.Serializable

interface InAppApiContext: ApiContext<InAppCall>, InAppConfig

interface InAppConfig {
    var inAppDnd: Boolean
}

class InAppContext(override val calls: MutableList<InAppCall>) : InAppApiContext {
    override var inAppDnd: Boolean = false
}

@Serializable
sealed interface InAppCall {

    @Serializable class Pause: InAppCall {
        override fun equals(other: Any?): Boolean {
            return other is Pause
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    @Serializable class Resume: InAppCall {
        override fun equals(other: Any?): Boolean {
            return other is Resume
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }
}