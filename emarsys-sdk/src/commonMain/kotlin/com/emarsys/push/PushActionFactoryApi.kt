package com.emarsys.push

import com.emarsys.action.Action
import com.emarsys.action.ExecutableCommand

interface PushActionFactoryApi {
    suspend fun create(action: Action): ExecutableCommand
}