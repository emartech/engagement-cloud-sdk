package com.emarsys.mobileengage.embedded.messages

internal open class EmbeddedMessagingPaginationState(
    open var lastFetchMessagesId: String? = null,
    open var top: Int = 0,
    open var offset: Int = 0,
    open var categoryIds: List<Int> = emptyList(),
    open var count: Int = 0
) {
    open fun canFetchNextPage(): Boolean {
        return if(top>=0 && offset>=0 && count>=0){
            (offset.plus(top)) < count
        } else false
    }

    open fun updateOffset() {
        if(offset>=0 && top>=0){
            offset = offset.plus(top)
        }
    }
}