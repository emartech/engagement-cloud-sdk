package com.emarsys.mobileengage.action.actions

import com.emarsys.core.message.Msg
import com.emarsys.core.message.MsgHubApi
import com.emarsys.mobileengage.action.models.DismissActionModel

class DismissAction(
    private val action: DismissActionModel,
    private val msgHub: MsgHubApi
): Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        action.msgBox?.let {
            msgHub.send(object: Msg<Unit> {
                override val content: Unit = Unit
            }, it)
        }
    }
}
