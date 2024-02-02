package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.action.Action
import com.emarsys.mobileengage.action.ExecutableCommand

interface InAppActionFactoryApi {
    suspend fun create(action: Action): ExecutableCommand
}