package com.emarsys.mobileengage.action.actions

interface Action<Value> {
    suspend operator fun invoke(value: Value? = null)
}
