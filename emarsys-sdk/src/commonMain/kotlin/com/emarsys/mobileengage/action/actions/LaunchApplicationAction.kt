package com.emarsys.mobileengage.action.actions

import com.emarsys.core.launchapplication.LaunchApplicationHandlerApi

class LaunchApplicationAction(private val launchApplicationHandler: LaunchApplicationHandlerApi) :
    Action<Unit> {

    override suspend fun invoke(value: Unit?) {
        launchApplicationHandler.launchApplication()
    }
}
