package com.emarsys.mobileengage.action.actions

import com.emarsys.mobileengage.action.models.ButtonClickedActionModel

class ButtonClickedAction(action: ButtonClickedActionModel) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        TODO("Handle button clicks from different sources: push, in-app")
    }
}