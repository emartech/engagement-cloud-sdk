package com.sap.ec.mobileengage.action.actions

interface Action<Value> {
    suspend operator fun invoke(value: Value? = null)
}
