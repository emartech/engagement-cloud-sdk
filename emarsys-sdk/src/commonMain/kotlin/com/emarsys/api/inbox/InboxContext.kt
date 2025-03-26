package com.emarsys.api.inbox

import com.emarsys.api.inbox.model.Message
import com.emarsys.di.SdkComponent
import kotlinx.serialization.Serializable

internal class InboxContext(override val calls: MutableList<InboxCall>) : InboxContextApi,
    SdkComponent {
    override val messages: MutableList<Message> = mutableListOf()
}

@Serializable
sealed interface InboxCall {

    @Serializable
    class FetchMessages : InboxCall {
        override fun equals(other: Any?): Boolean {
            return other is FetchMessages
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    @Serializable
    data class AddTag(val tag: String, val messageId: String) : InboxCall

    @Serializable
    data class RemoveTag(val tag: String, val messageId: String) : InboxCall
}