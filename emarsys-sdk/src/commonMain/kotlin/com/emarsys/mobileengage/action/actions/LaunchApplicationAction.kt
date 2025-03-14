package com.emarsys.mobileengage.action.actions

import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi

class LaunchApplicationAction(private val launchApplicationHandler: LaunchApplicationHandlerApi) :
    Action<Unit> {

    override suspend fun invoke(value: Unit?) {
        launchApplicationHandler.launchApplication()
    }
}
