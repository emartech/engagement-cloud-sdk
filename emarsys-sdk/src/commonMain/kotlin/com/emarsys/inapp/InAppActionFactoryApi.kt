package com.emarsys.inapp

import com.emarsys.action.Action
import com.emarsys.action.ExecutableCommand

interface InAppActionFactoryApi {
    suspend fun create(action: Action): ExecutableCommand
}