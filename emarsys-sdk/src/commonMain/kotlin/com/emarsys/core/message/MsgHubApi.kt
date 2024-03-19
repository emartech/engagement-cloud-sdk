package com.emarsys.core.message

typealias MsgCallback<Content> = (Msg<Content>) -> Unit


interface MsgDispatcherApi<Content> {

    val msgBox: MsgBox<Content>

    fun send(msg: Msg<Content>)

    fun enrollWith(onMsg: MsgCallback<Content>)

    fun unEnrollWith(onMsg: MsgCallback<Content>)

}

interface MsgHubApi {

    fun <Content>open(msgBox: MsgBox<Content>)

    fun <Content>close(msgBox: MsgBox<Content>)

    fun <Content>send(msg: Msg<Content>, to: MsgBox<Content>)

    fun <Content>enrollFor(msgBox: MsgBox<Content>, onMsg: MsgCallback<Content>)

    fun <Content>unEnrollFor(msgBox: MsgBox<Content>, onMsg: MsgCallback<Content>)

}

interface Msg<Content> {

    val content: Content
}

interface MsgBox<Content> {

    val id: String

    val replayCount: Int
}
