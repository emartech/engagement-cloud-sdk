package com.emarsys.api.inbox

import com.emarsys.api.inbox.model.Message

internal interface InboxContextApi {
    val calls: MutableList<InboxCall>
    val messages: MutableList<Message>
}