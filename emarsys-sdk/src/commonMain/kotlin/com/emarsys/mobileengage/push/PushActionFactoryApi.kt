package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.Action
import com.emarsys.mobileengage.action.ExecutableCommand

interface PushActionFactoryApi {
    suspend fun create(action: Action): ExecutableCommand
}