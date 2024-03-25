package com.emarsys.core.message

typealias MsgCallback<Content> = (content: Content) -> Unit

interface MsgHubApi {

    suspend fun send(msg: Any, to: String)

    suspend fun subscribe(topic: String, onMsg: MsgCallback<Any>)

}
