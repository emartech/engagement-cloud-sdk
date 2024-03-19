package com.emarsys.mobileengage.action.actions

sealed interface Action<Value> {
    suspend operator fun invoke(value: Value? = null)
}
