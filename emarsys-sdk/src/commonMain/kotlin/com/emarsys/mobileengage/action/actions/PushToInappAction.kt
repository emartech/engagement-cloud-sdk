package com.emarsys.mobileengage.action.actions


class PushToInappAction: Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        println("PushToInappAction invoked")
    }
}