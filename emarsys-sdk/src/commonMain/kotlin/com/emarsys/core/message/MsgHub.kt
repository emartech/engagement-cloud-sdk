package com.emarsys.core.message

class MsgHub: MsgHubApi {

    private val msgBoxes = mutableMapOf<MsgBox<*>, MsgDispatcher<*>>()
    override fun <Content> open(msgBox: MsgBox<Content>) {
        require(msgBoxes[msgBox] == null) { "MsgBox is already open" }
        msgBoxes[msgBox] = MsgDispatcher(msgBox)
    }

    override fun <Content> close(msgBox: MsgBox<Content>) {
        require(msgBoxes[msgBox] != null) { "MsgBox is not open" }
        msgBoxes.remove(msgBox)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <Content> send(msg: Msg<Content>, to: MsgBox<Content>) {
        require(msgBoxes[to] != null) { "MsgBox is not open" }
        (msgBoxes[to] as MsgDispatcher<Content>).send(msg)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <Content> enrollFor(msgBox: MsgBox<Content>, onMsg: MsgCallback<Content>) {
        require(msgBoxes[msgBox] != null) { "MsgBox is not open" }
        (msgBoxes[msgBox] as MsgDispatcher<Content>).enrollWith(onMsg)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <Content> unEnrollFor(msgBox: MsgBox<Content>, onMsg: MsgCallback<Content>) {
        require(msgBoxes[msgBox] != null) { "MsgBox is not open" }
        (msgBoxes[msgBox] as MsgDispatcher<Content>).unEnrollWith(onMsg)
    }

}

private class MsgDispatcher<Content>(override val msgBox: MsgBox<Content>): MsgDispatcherApi<Content> {

    val msgCallbacks = mutableListOf<MsgCallback<Content>>()
    var msgQueue = mutableListOf<Msg<Content>>()
    override fun send(msg: Msg<Content>) {
        msgQueue.add(msg)
        msgCallbacks.forEach { callback -> callback.invoke(msg) }
    }

    override fun enrollWith(onMsg: MsgCallback<Content>) {
        if (msgBox.replayCount > 0) {
            msgQueue = msgQueue.takeLast(msgBox.replayCount).toMutableList()
            msgQueue.forEach { msg ->
                onMsg.invoke(msg)
            }
        }
        msgCallbacks.add(onMsg)
    }

    override fun unEnrollWith(onMsg: MsgCallback<Content>) {
        msgCallbacks.remove(onMsg)
    }

}
