package com.emarsys.core.message

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MsgHub(private val dispatcher: CoroutineDispatcher): MsgHubApi {

    private val flow = MutableSharedFlow<Any>()

    override suspend fun send(msg: Any, to: String) {
        flow.emit(Pair(to, msg))
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun subscribe(topic: String, onMsg: MsgCallback<Any>) {
        CoroutineScope(dispatcher).launch {
            flow.collect {
                if (it is Pair<*, *> && it.first == topic) {
                    onMsg(it.second as Any)
                }
            }
        }
    }

}
