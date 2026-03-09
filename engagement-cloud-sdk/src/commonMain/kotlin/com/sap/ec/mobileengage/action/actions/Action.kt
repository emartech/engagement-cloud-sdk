package com.sap.ec.mobileengage.action.actions

internal interface Action<Value> {
    suspend operator fun invoke(value: Value? = null)
}
