package com.sap.ec.mobileengage.action.actions

import com.sap.ec.core.actions.launchapplication.LaunchApplicationHandlerApi

class LaunchApplicationAction(private val launchApplicationHandler: LaunchApplicationHandlerApi) :
    Action<Unit> {

    override suspend fun invoke(value: Unit?) {
        launchApplicationHandler.launchApplication()
    }
}
