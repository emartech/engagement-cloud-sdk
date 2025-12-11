package com.emarsys.mobileengage.embeddedmessaging.pagination

import com.emarsys.core.Registerable

interface EmbeddedMessagingPaginationHandlerApi : Registerable {

    fun isEndReached(): Boolean

}